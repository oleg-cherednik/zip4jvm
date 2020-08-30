package ru.olegcherednik.zip4jvm.io.out.data;

import lombok.Getter;
import ru.olegcherednik.zip4jvm.io.writers.ZipModelWriter;
import ru.olegcherednik.zip4jvm.model.DataDescriptor;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;
import ru.olegcherednik.zip4jvm.utils.ValidationUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 08.03.2019
 */
@Getter
public class SplitZipOutputStream extends BaseZipDataOutput {

    /** see 8.5.5 */
    public static final int SPLIT_SIGNATURE = DataDescriptor.SIGNATURE;

    private int diskNo;

    public SplitZipOutputStream(ZipModel zipModel) throws IOException {
        super(zipModel);
        ValidationUtils.requireZeroOrPositive(zipModel.getSplitSize(), "zipModel.splitSize");
        writeDwordSignature(SPLIT_SIGNATURE);
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
        long available = zipModel.getSplitSize() - getRelativeOffs();

        if (available <= len)
            openNextDisk();
    }

    @Override
    @SuppressWarnings("PMD.AvoidReassigningParameters")
    public void write(byte[] buf, int offs, int len) throws IOException {
        final int offsInit = offs;

        while (len > 0) {
            long available = zipModel.getSplitSize() - getRelativeOffs();

            if (available <= 0 || len > available && offsInit != offs)
                openNextDisk();

            available = zipModel.getSplitSize() - getRelativeOffs();

            int writeBytes = Math.min(len, (int)available);
            super.write(buf, offs, writeBytes);

            offs += writeBytes;
            len -= writeBytes;
        }
    }

    private void openNextDisk() throws IOException {
        super.close();

        SrcZip srcZip = zipModel.getSrcZip();
        Path path = srcZip.getPath();
        Path diskPath = srcZip.getDiskPath(++diskNo);

        // TODO #34 - Validate all new create split disks are not exist
        if (Files.exists(diskPath))
            throw new IOException("split file: " + diskPath.getFileName() + " already exists in the current directory, cannot rename this file");

        if (!path.toFile().renameTo(diskPath.toFile()))
            throw new IOException("cannot rename newly created split file");

        createFile(path);
    }

    @Override
    public void close() throws IOException {
        new ZipModelWriter(zipModel).write(this);
        super.close();
    }

    @Override
    public final String toString() {
        return super.toString() + "; disk: " + diskNo;
    }

}
