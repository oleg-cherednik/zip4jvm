package com.cop.zip4j.io.in;

import com.cop.zip4j.exception.Zip4jException;
import com.cop.zip4j.io.out.entry.EntryOutputStream;
import com.cop.zip4j.model.ZipModel;
import lombok.NonNull;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 04.08.2019
 */
public class SplitZipInputStream extends BaseMarkDataInput {

    private int counter;

    @NonNull
    public static SplitZipInputStream create(@NonNull ZipModel zipModel, int diskNumber) throws IOException {
        return new SplitZipInputStream(zipModel, diskNumber);
    }

    private SplitZipInputStream(@NonNull ZipModel zipModel, int diskNumber) throws IOException {
        super(zipModel);
        counter = diskNumber + 1;
        delegate = new LittleEndianReadFile(zipModel.getPartFile(diskNumber));
        checkSignature();
    }

    private void checkSignature() throws IOException {
        if (counter != 1)
            return;

        int signature = delegate.readDword();

        if (signature != EntryOutputStream.SPLIT_SIGNATURE)
            throw new Zip4jException("Incorrect split file signature: " + zipModel.getZipFile().getFileName());
    }

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
//        if (len > length - bytesRead) {
//            len = (int)(length - bytesRead);
//
//            if (len == 0) {
//                checkAndReadAESMacBytes();
//                checkAndReadAESNewMacBytes();
//                return -1;
//            }
//        }
//
//        len = decoder.getLen(bytesRead, len, length);

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
        Path splitFile = zipModel.getPartFile(counter++);
        delegate.close();
        delegate = new LittleEndianReadFile(splitFile);
    }

    private DataInput openNextSplitOld() throws IOException {
        Path currSplitFile = zipModel.getZipFile();

        if (counter != zipModel.getEndCentralDirectory().getSplitParts())
            currSplitFile = ZipModel.getSplitFilePath(currSplitFile, counter + 1);

        if (!Files.exists(currSplitFile))
            throw new Zip4jException("split file: " + currSplitFile.getFileName() + " does not exists");

        counter++;
        return new LittleEndianReadFile(currSplitFile);
    }

    @Override
    public int getCounter() {
        return 0;
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

}
