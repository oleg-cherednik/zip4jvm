package net.lingala.zip4j.io;

import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipModel;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 08.03.2019
 */
public class NoSplitOutputStream extends SplitOutputStream {
    public NoSplitOutputStream(Path file) throws FileNotFoundException, ZipException {
        super(file, ZipModel.NO_SPLIT);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (len <= 0)
            return;

        raf.write(b, off, len);
        bytesWrittenForThisPart += len;
    }

    @Override
    public boolean isBuffSizeFitForCurrSplitFile(int bufferSize) throws ZipException {
        return true;
    }

    @Override
    public boolean isSplitZipFile() {
        return false;
    }
}
