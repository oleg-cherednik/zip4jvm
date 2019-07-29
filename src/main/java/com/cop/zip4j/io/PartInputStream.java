package com.cop.zip4j.io;

import com.cop.zip4j.crypto.aes.AesDecoder;
import com.cop.zip4j.crypto.aes.AesEngine;
import com.cop.zip4j.engine.UnzipEngine;
import com.cop.zip4j.model.Encryption;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

public class PartInputStream extends InputStream {

    private RandomAccessFile in;
    private final long length;
    private final UnzipEngine unzipEngine;

    private long bytesRead;
    private byte[] oneByteBuff = new byte[1];
    private byte[] aesBlockByte = new byte[16];
    private int aesBytesReturned = 0;
    private int count = -1;

    public PartInputStream(LittleEndianRandomAccessFile in, long length, UnzipEngine unzipEngine) {
        this.in = in.getIn();
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

        if (unzipEngine.getDecoder() instanceof AesDecoder) {
            if (bytesRead + len < length) {
                if (len % 16 != 0) {
                    len = len - (len % 16);
                }
            }
        }

        synchronized (in) {
            count = in.read(b, off, len);
            if ((count < len) && unzipEngine.getZipModel().isSplitArchive()) {
                in.close();
                in = unzipEngine.startNextSplitFile();
                if (count < 0) count = 0;
                int newlyRead = in.read(b, count, len - count);
                if (newlyRead > 0)
                    count += newlyRead;
            }
        }

        if (count > 0) {
            if (unzipEngine.getDecoder() != null)
                unzipEngine.getDecoder().decrypt(b, off, count);
            bytesRead += count;
        }

        if (bytesRead >= length)
            checkAndReadAESMacBytes();

        return count;
    }

    protected void checkAndReadAESMacBytes() throws IOException {
        if (!isAes())
            return;
        if (unzipEngine.getDecoder() == null || !(unzipEngine.getDecoder() instanceof AesDecoder))
            return;

        if (((AesDecoder)unzipEngine.getDecoder()).getStoredMac() != null) {
            //Stored mac already set
            return;
        }
        byte[] macBytes = new byte[AesEngine.AES_AUTH_LENGTH];
        int readLen = -1;
        readLen = in.read(macBytes);
        if (readLen != AesEngine.AES_AUTH_LENGTH) {
            if (unzipEngine.getZipModel().isSplitArchive()) {
                in.close();
                in = unzipEngine.startNextSplitFile();
                int newlyRead = in.read(macBytes, readLen, AesEngine.AES_AUTH_LENGTH - readLen);
                readLen += newlyRead;
            } else {
                throw new IOException("Error occured while reading stored AES authentication bytes");
            }
        }

        ((AesDecoder)unzipEngine.getDecoder()).setStoredMac(macBytes);
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
        in.close();
    }

}
