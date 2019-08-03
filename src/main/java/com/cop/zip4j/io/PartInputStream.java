package com.cop.zip4j.io;

import com.cop.zip4j.crypto.Decoder;
import com.cop.zip4j.crypto.aes.AesDecoder;
import com.cop.zip4j.crypto.aes.AesEngine;
import com.cop.zip4j.crypto.aesnew.AesNewDecoder;
import com.cop.zip4j.engine.UnzipEngine;
import com.cop.zip4j.io.in.LittleEndianReadFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

public class PartInputStream extends InputStream {

    private RandomAccessFile in;
    private final long length;
    private final UnzipEngine engine;
    private final Decoder decoder;


    private long bytesRead;
    private byte[] oneByteBuff = new byte[1];
    private byte[] aesBlockByte = new byte[16];
    private int aesBytesReturned = 0;

    public PartInputStream(LittleEndianReadFile in, long length, Decoder decoder, UnzipEngine engine) {
        this.in = in.getIn();
        this.engine = engine;
        this.length = length;
        this.decoder = decoder;
    }

    @Override
    public int available() {
        return (int)Math.min(length - bytesRead, Integer.MAX_VALUE);
    }

    @Override
    public int read() throws IOException {
        if (bytesRead >= length)
            return -1;

        if (!(decoder instanceof AesDecoder || decoder instanceof AesNewDecoder))
            return read(oneByteBuff, 0, 1) == -1 ? -1 : oneByteBuff[0] & 0xFF;

        if (aesBytesReturned == 0 || aesBytesReturned == 16) {
            if (read(aesBlockByte) == -1)
                return -1;

            aesBytesReturned = 0;
        }

        return aesBlockByte[aesBytesReturned++] & 0xff;
    }

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        if (len > length - bytesRead) {
            len = (int)(length - bytesRead);

            if (len == 0) {
                checkAndReadAESMacBytes();
                checkAndReadAESNewMacBytes();
                return -1;
            }
        }

        len = decoder.getLen(bytesRead, len, length);


        int count = in.read(buf, offs, len);

        if ((count < len) && engine.getZipModel().isSplitArchive()) {
            in.close();
            in = engine.startNextSplitFile();

            if (count < 0)
                count = 0;

            // TODO what if next file is still small for input?
            int newlyRead = in.read(buf, count, len - count);

            if (newlyRead > 0)
                count += newlyRead;
        }

        if (count > 0) {
            decoder.decrypt(buf, offs, count);
            bytesRead += count;
        }

        if (bytesRead >= length) {
            checkAndReadAESMacBytes();
            checkAndReadAESNewMacBytes();
        }

        return count;
    }

    protected void checkAndReadAESMacBytes() throws IOException {
        if (!(decoder instanceof AesNewDecoder))
            return;

        AesNewDecoder dec = (AesNewDecoder)decoder;

        if (dec.getMacKey() == null)
            dec.setMacKey(readMac());
    }

    protected void checkAndReadAESNewMacBytes() throws IOException {
        if (!(decoder instanceof AesNewDecoder))
            return;

        AesNewDecoder dec = (AesNewDecoder)decoder;

        if (dec.getMacKey() == null)
            dec.setMacKey(readMac());
    }

    private byte[] readMac() throws IOException {
        byte[] mac = new byte[AesEngine.AES_AUTH_LENGTH];
        int readLen = in.read(mac);

        if (readLen != AesEngine.AES_AUTH_LENGTH) {
            if (engine.getZipModel().isSplitArchive())
                throw new IOException("Error occured while reading stored AES authentication bytes");

            in.close();
            // TODO what if more than one file
            in = engine.startNextSplitFile();
            in.read(mac, readLen, AesEngine.AES_AUTH_LENGTH - readLen);
        }

        return mac;
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
