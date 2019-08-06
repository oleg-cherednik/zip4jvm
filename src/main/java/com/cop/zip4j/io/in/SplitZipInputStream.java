package com.cop.zip4j.io.in;

import com.cop.zip4j.exception.Zip4jException;
import com.cop.zip4j.io.out.entry.EntryOutputStream;
import com.cop.zip4j.model.ZipModel;
import lombok.NonNull;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 04.08.2019
 */
public class SplitZipInputStream extends BaseMarkDataInput {

    private int diskNumber;

    @NonNull
    public static SplitZipInputStream create(@NonNull ZipModel zipModel, int diskNumber) throws IOException {
        return new SplitZipInputStream(zipModel, diskNumber);
    }

    private SplitZipInputStream(@NonNull ZipModel zipModel, int diskNumber) throws IOException {
        super(zipModel);
        this.diskNumber = diskNumber;
        delegate = new LittleEndianReadFile(zipModel.getPartFile(diskNumber));
        checkSignature();
    }

    private void checkSignature() throws IOException {
        if (diskNumber != 0)
            return;

        int signature = delegate.readDword();

        if (signature != EntryOutputStream.SPLIT_SIGNATURE)
            throw new Zip4jException("Incorrect split file signature: " + zipModel.getZipFile().getFileName());
    }

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        int res = 0;

        while (res < len) {
            int total = delegate.read(buf, offs, len);

            if (total > 0) {
                res += total;
            }

            if (total == IOUtils.EOF || total < len) {
                openNextSplit();
                offs += Math.max(0, total);
                len -= Math.max(0, total);
            }
        }

        return res;
    }

    private void openNextSplit() throws IOException {
        Path splitFile = zipModel.getPartFile(++diskNumber);
        delegate.close();
        delegate = new LittleEndianReadFile(splitFile);
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

}
