package ru.olegcherednik.zip4jvm.io.out;

import lombok.Getter;
import lombok.NonNull;
import ru.olegcherednik.zip4jvm.exception.Zip4jException;
import ru.olegcherednik.zip4jvm.io.writers.ZipModelWriter;
import ru.olegcherednik.zip4jvm.model.DataDescriptor;
import ru.olegcherednik.zip4jvm.model.ZipModel;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 08.03.2019
 */
@Getter
public class SplitZipOutputStream extends BaseDataOutput {

    /** see 8.5.5 */
    public static final int SPLIT_SIGNATURE = DataDescriptor.SIGNATURE;

    private long disk;

    public static SplitZipOutputStream create(@NonNull ZipModel zipModel) throws IOException {
        // TODO move to ZipParameters
        if (zipModel.getSplitSize() >= 0 && zipModel.getSplitSize() < ZipModel.MIN_SPLIT_SIZE)
            throw new Zip4jException("split length less than minimum allowed split length of " + ZipModel.MIN_SPLIT_SIZE + " Bytes");

        SplitZipOutputStream out = new SplitZipOutputStream(zipModel);
        out.writeDwordSignature(SPLIT_SIGNATURE);

        // TODO it will not work for existed zip archive
        if (zipModel.getCentralDirectoryOffs() == 0)
            zipModel.setCentralDirectoryOffs(out.getOffs());

        return out;
    }

    private SplitZipOutputStream(ZipModel zipModel) throws FileNotFoundException {
        super(zipModel);
        createFile(zipModel.getStreamFile());
    }

    @Override
    public void writeWordSignature(int sig) throws IOException {
        doNotSplitSignature(2);
        super.writeWordSignature(sig);
    }

    @Override
    public void writeDwordSignature(int sig) throws IOException {
        doNotSplitSignature(4);
        super.writeDwordSignature(sig);
    }

    private void doNotSplitSignature(int len) throws IOException {
        long available = zipModel.getSplitSize() - getOffs();

        if (available <= len)
            openNextDisk();
    }

    @Override
    public void write(byte[] buf, int offs, int len) throws IOException {
        final int offsInit = offs;

        while (len > 0) {
            long available = zipModel.getSplitSize() - getOffs();
            int writeBytes = Math.min(len, (int)available);

            if (available <= 0 || len > available && offsInit != offs)
                openNextDisk();

            super.write(buf, offs, writeBytes);

            offs += writeBytes;
            len -= writeBytes;
        }
    }

    private void openNextDisk() throws IOException {
        Path splitFile = ZipModel.getSplitFilePath(zipModel.getStreamFile(), ++disk);

        super.close();

        if (Files.exists(splitFile))
            throw new IOException("split file: " + splitFile.getFileName() + " already exists in the current directory, cannot rename this file");

        if (!zipModel.getStreamFile().toFile().renameTo(splitFile.toFile()))
            throw new IOException("cannot rename newly created split file");

        createFile(zipModel.getStreamFile());
    }

    @Override
    public void close() throws IOException {
        new ZipModelWriter(zipModel).write(this);
        super.close();
    }

}
