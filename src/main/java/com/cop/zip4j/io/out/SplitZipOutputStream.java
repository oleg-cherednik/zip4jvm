package com.cop.zip4j.io.out;

import com.cop.zip4j.exception.Zip4jException;
import com.cop.zip4j.io.writers.ZipModelWriter;
import com.cop.zip4j.model.DataDescriptor;
import com.cop.zip4j.model.ZipModel;
import lombok.NonNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 08.03.2019
 */
public class SplitZipOutputStream extends BaseDataOutput {

    /** see 8.5.5 */
    public static final int SPLIT_SIGNATURE = DataDescriptor.SIGNATURE;

    private int disK;

    @NonNull
    public static SplitZipOutputStream create(@NonNull ZipModel zipModel) throws IOException {
        // TODO move to ZipParameters
        if (zipModel.getSplitSize() >= 0 && zipModel.getSplitSize() < ZipModel.MIN_SPLIT_LENGTH)
            throw new Zip4jException("split length less than minimum allowed split length of " + ZipModel.MIN_SPLIT_LENGTH + " Bytes");

        SplitZipOutputStream out = new SplitZipOutputStream(zipModel);
        out.writeDwordSignature(SPLIT_SIGNATURE);
        return out;
    }

    private SplitZipOutputStream(@NonNull ZipModel zipModel) throws FileNotFoundException {
        super(zipModel);
        createFile(zipModel.getZipFile());
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
        Path splitFile = ZipModel.getSplitFilePath(zipModel.getZipFile(), ++disK);

        super.close();

        if (Files.exists(splitFile))
            throw new IOException("split file: " + splitFile.getFileName() + " already exists in the current directory, cannot rename this file");

        if (!zipModel.getZipFile().toFile().renameTo(splitFile.toFile()))
            throw new IOException("cannot rename newly created split file");

        createFile(zipModel.getZipFile());
    }

    @Override
    public int getDisk() {
        return disK;
    }

    @Override
    public void close() throws IOException {
        new ZipModelWriter(zipModel).write(this);
        super.close();
    }

}
