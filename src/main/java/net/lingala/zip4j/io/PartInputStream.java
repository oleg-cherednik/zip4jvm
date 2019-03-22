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

import net.lingala.zip4j.crypto.AESDecrypter;
import net.lingala.zip4j.engine.UnzipEngine;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.Encryption;
import net.lingala.zip4j.util.InternalZipConstants;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

public class PartInputStream extends InputStream {

    private RandomAccessFile raf;
    private final long length;
    private final UnzipEngine unzipEngine;

    private long bytesRead;
    private byte[] oneByteBuff = new byte[1];
    private byte[] aesBlockByte = new byte[16];
    private int aesBytesReturned = 0;
    private int count = -1;

    public PartInputStream(RandomAccessFile raf, long length, UnzipEngine unzipEngine) {
        this.raf = raf;
        this.unzipEngine = unzipEngine;
        this.length = length;
    }

    private boolean isAes() {
        return unzipEngine.getFileHeader().getEncryption() == Encryption.AES;
    }

    @Override
    public int available() {
        return (int)Math.min(length - bytesRead, Integer.MAX_VALUE);
    }

    @Override
    public int read() throws IOException {
        if (bytesRead >= length)
            return -1;

        if (!isAes())
            return read(oneByteBuff, 0, 1) == -1 ? -1 : oneByteBuff[0] & 0xFF;

        if (aesBytesReturned == 0 || aesBytesReturned == 16) {
            if (read(aesBlockByte) == -1)
                return -1;

            aesBytesReturned = 0;
        }

        return aesBlockByte[aesBytesReturned++] & 0xff;
    }

    public int read(byte[] b, int off, int len) throws IOException {
        if (len > length - bytesRead) {
            len = (int)(length - bytesRead);
            if (len == 0) {
                checkAndReadAESMacBytes();
                return -1;
            }
        }

        if (unzipEngine.getDecrypter() instanceof AESDecrypter) {
            if (bytesRead + len < length) {
                if (len % 16 != 0) {
                    len = len - (len % 16);
                }
            }
        }

        synchronized (raf) {
            count = raf.read(b, off, len);
            if ((count < len) && unzipEngine.getZipModel().isSplitArchive()) {
                raf.close();
                raf = unzipEngine.startNextSplitFile();
                if (count < 0) count = 0;
                int newlyRead = raf.read(b, count, len - count);
                if (newlyRead > 0)
                    count += newlyRead;
            }
        }

        if (count > 0) {
            if (unzipEngine.getDecrypter() != null) {
                try {
                    unzipEngine.getDecrypter().decryptData(b, off, count);
                } catch(ZipException e) {
                    throw new IOException(e.getMessage());
                }
            }
            bytesRead += count;
        }

        if (bytesRead >= length)
            checkAndReadAESMacBytes();

        return count;
    }

    protected void checkAndReadAESMacBytes() throws IOException {
        if (!isAes())
            return;
        if (unzipEngine.getDecrypter() == null || !(unzipEngine.getDecrypter() instanceof AESDecrypter))
            return;

        if (((AESDecrypter)unzipEngine.getDecrypter()).getStoredMac() != null) {
            //Stored mac already set
            return;
        }
        byte[] macBytes = new byte[InternalZipConstants.AES_AUTH_LENGTH];
        int readLen = -1;
        readLen = raf.read(macBytes);
        if (readLen != InternalZipConstants.AES_AUTH_LENGTH) {
            if (unzipEngine.getZipModel().isSplitArchive()) {
                raf.close();
                raf = unzipEngine.startNextSplitFile();
                int newlyRead = raf.read(macBytes, readLen, InternalZipConstants.AES_AUTH_LENGTH - readLen);
                readLen += newlyRead;
            } else {
                throw new IOException("Error occured while reading stored AES authentication bytes");
            }
        }

        ((AESDecrypter)unzipEngine.getDecrypter()).setStoredMac(macBytes);
    }

    @Override
    public long skip(long n) throws IOException {
        if (n < 0)
            throw new IllegalArgumentException();
        if (n > length - bytesRead)
            n = length - bytesRead;
        bytesRead += n;
        return n;
    }

    @Override
    public void close() throws IOException {
        raf.close();
    }

}
