package com.cop.zip4j.io.out;

import com.cop.zip4j.io.writers.ZipModelWriter;
import com.cop.zip4j.exception.Zip4jException;
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

    private int counter;

    @NonNull
    public static SplitZipOutputStream create(@NonNull ZipModel zipModel) throws FileNotFoundException {
        // TODO move to ZipParameters
        if (zipModel.getSplitLength() >= 0 && zipModel.getSplitLength() < ZipModel.MIN_SPLIT_LENGTH)
            throw new Zip4jException("split length less than minimum allowed split length of " + ZipModel.MIN_SPLIT_LENGTH + " Bytes");

        return new SplitZipOutputStream(zipModel);
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
        long available = zipModel.getSplitLength() - getOffs();

        if (available <= len)
            openNextSplit();
    }

    @Override
    public void write(byte[] buf, int offs, int len) throws IOException {
        final int offsInit = offs;

        while (len > 0) {
            long available = zipModel.getSplitLength() - getOffs();
            int writeBytes = Math.min(len, (int)available);

            if (available <= 0 || len > available && offsInit != offs)
                openNextSplit();

            super.write(buf, offs, writeBytes);

            offs += writeBytes;
            len -= writeBytes;
        }
    }

    private void openNextSplit() throws IOException {
        Path splitFile = ZipModel.getSplitFilePath(zipModel.getZipFile(), ++counter);

        super.close();

        if (Files.exists(splitFile))
            throw new IOException("split file: " + splitFile.getFileName() + " already exists in the current directory, cannot rename this file");

        if (!zipModel.getZipFile().toFile().renameTo(splitFile.toFile()))
            throw new IOException("cannot rename newly created split file");

        createFile(zipModel.getZipFile());
    }

    @Override
    public int getCounter() {
        return counter;
    }

    @Override
    public void close() throws IOException {
        new ZipModelWriter(zipModel).write(this);
        super.close();
    }

}
