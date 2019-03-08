/*
 * Copyright 2010 Srikanth Reddy Lingala
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.lingala.zip4j.io;

import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.util.InternalZipConstants;
import net.lingala.zip4j.util.Raw;
import net.lingala.zip4j.util.Zip4jUtil;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Path;

public class SplitOutputStream extends OutputStream {

    private RandomAccessFile raf;
    private long splitLength;
    private Path zipFile;
    private Path outFile;
    private int currSplitFileCounter;
    private long bytesWrittenForThisPart;

    public SplitOutputStream(Path file) throws FileNotFoundException, ZipException {
        this(file, -1);
    }

    public SplitOutputStream(Path file, long splitLength) throws FileNotFoundException, ZipException {
        if (splitLength >= 0 && splitLength < InternalZipConstants.MIN_SPLIT_LENGTH) {
            throw new ZipException("split length less than minimum allowed split length of " + InternalZipConstants.MIN_SPLIT_LENGTH + " Bytes");
        }
        raf = new RandomAccessFile(file.toFile(), InternalZipConstants.WRITE_MODE);
        this.splitLength = splitLength;
        outFile = file;
        zipFile = file;
        currSplitFileCounter = 0;
        bytesWrittenForThisPart = 0;
    }

    public void write(int b) throws IOException {
        byte[] buff = new byte[1];
        buff[0] = (byte)b;
        write(buff, 0, 1);
    }

    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        if (len <= 0) return;

        if (splitLength != -1) {

            if (splitLength < InternalZipConstants.MIN_SPLIT_LENGTH) {
                throw new IOException("split length less than minimum allowed split length of " + InternalZipConstants.MIN_SPLIT_LENGTH + " Bytes");
            }

            if (bytesWrittenForThisPart >= splitLength) {
                startNextSplitFile();
                raf.write(b, off, len);
                bytesWrittenForThisPart = len;
            } else if (bytesWrittenForThisPart + len > splitLength) {
                if (isHeaderData(b)) {
                    startNextSplitFile();
                    raf.write(b, off, len);
                    bytesWrittenForThisPart = len;
                } else {
                    raf.write(b, off, (int)(splitLength - bytesWrittenForThisPart));
                    startNextSplitFile();
                    raf.write(b, off + (int)(splitLength - bytesWrittenForThisPart), (int)(len - (splitLength - bytesWrittenForThisPart)));
                    bytesWrittenForThisPart = len - (splitLength - bytesWrittenForThisPart);
                }
            } else {
                raf.write(b, off, len);
                bytesWrittenForThisPart += len;
            }

        } else {
            raf.write(b, off, len);
            bytesWrittenForThisPart += len;
        }

    }

    private void startNextSplitFile() throws IOException {
        String zipFileWithoutExt = FilenameUtils.getBaseName(outFile.getFileName().toString());
        File currSplitFile = null;
        String zipFileName = zipFile.toAbsolutePath().toString();
        String parentPath = (outFile.getParent() == null) ? "" : outFile.getParent() + System.getProperty("file.separator");

        if (currSplitFileCounter < 9) {
            currSplitFile = new File(parentPath + zipFileWithoutExt + ".z0" + (currSplitFileCounter + 1));
        } else {
            currSplitFile = new File(parentPath + zipFileWithoutExt + ".z" + (currSplitFileCounter + 1));
        }

        raf.close();

        if (currSplitFile.exists()) {
            throw new IOException("split file: " + currSplitFile.getName() + " already exists in the current directory, cannot rename this file");
        }

        if (!zipFile.toFile().renameTo(currSplitFile)) {
            throw new IOException("cannot rename newly created split file");
        }

        zipFile = new File(zipFileName).toPath();
        raf = new RandomAccessFile(zipFile.toFile(), InternalZipConstants.WRITE_MODE);
        currSplitFileCounter++;
    }

    private boolean isHeaderData(byte[] buff) {
        if (buff == null || buff.length < 4) {
            return false;
        }

        int signature = Raw.readIntLittleEndian(buff, 0);
        long[] allHeaderSignatures = Zip4jUtil.getAllHeaderSignatures();
        if (allHeaderSignatures != null && allHeaderSignatures.length > 0) {
            for (int i = 0; i < allHeaderSignatures.length; i++) {
                //Ignore split signature
                if (allHeaderSignatures[i] != InternalZipConstants.SPLITSIG &&
                        allHeaderSignatures[i] == signature) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Checks if the buffer size is sufficient for the current split file. If not
     * a new split file will be started.
     *
     * @param bufferSize
     * @return true if a new split file was started else false
     * @throws ZipException
     */
    public boolean checkBuffSizeAndStartNextSplitFile(int bufferSize) throws ZipException {
        if (bufferSize < 0) {
            throw new ZipException("negative buffersize for checkBuffSizeAndStartNextSplitFile");
        }

        if (!isBuffSizeFitForCurrSplitFile(bufferSize)) {
            try {
                startNextSplitFile();
                bytesWrittenForThisPart = 0;
                return true;
            } catch(IOException e) {
                throw new ZipException(e);
            }
        }

        return false;
    }

    /**
     * Checks if the given buffer size will be fit in the current split file.
     * If this output stream is a non-split file, then this method always returns true
     *
     * @param bufferSize
     * @return true if the buffer size is fit in the current split file or else false.
     * @throws ZipException
     */
    public boolean isBuffSizeFitForCurrSplitFile(int bufferSize) throws ZipException {
        if (bufferSize < 0) {
            throw new ZipException("negative buffersize for isBuffSizeFitForCurrSplitFile");
        }

        if (splitLength >= InternalZipConstants.MIN_SPLIT_LENGTH) {
            return (bytesWrittenForThisPart + bufferSize <= splitLength);
        } else {
            //Non split zip -- return true
            return true;
        }
    }

    public void seek(long pos) throws IOException {
        raf.seek(pos);
    }

    public void close() throws IOException {
        if (raf != null)
            raf.close();
    }

    public void flush() throws IOException {
    }

    public long getFilePointer() throws IOException {
        return raf.getFilePointer();
    }

    public boolean isSplitZipFile() {
        return splitLength != -1;
    }

    public long getSplitLength() {
        return splitLength;
    }

    public int getCurrSplitFileCounter() {
        return currSplitFileCounter;
    }
}
