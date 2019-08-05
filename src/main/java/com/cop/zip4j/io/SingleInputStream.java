package com.cop.zip4j.io;

import com.cop.zip4j.crypto.Decoder;
import com.cop.zip4j.crypto.aes.AesDecoder;
import com.cop.zip4j.crypto.aes.AesEngine;
import com.cop.zip4j.crypto.aesnew.AesNewDecoder;
import com.cop.zip4j.exception.Zip4jException;
import com.cop.zip4j.io.in.DataInput;
import com.cop.zip4j.io.in.LittleEndianReadFile;
import com.cop.zip4j.model.ZipModel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@RequiredArgsConstructor
public class SingleInputStream extends InputStream {

    @NonNull
    private DataInput in;
    private final long length;
    private final Decoder decoder;
    private final ZipModel zipModel;

    private long bytesRead;
    private byte[] oneByteBuff = new byte[1];
    private byte[] aesBlockByte = new byte[16];
    private int aesBytesReturned = 0;

    @Override
    public int available() {
        return (int)Math.min(length - bytesRead, Integer.MAX_VALUE);
    }

    @Override
    public int read() throws IOException {
        if (bytesRead >= length)
            return -1;

        if (decoder instanceof AesDecoder || decoder instanceof AesNewDecoder) {
            if (aesBytesReturned == 0 || aesBytesReturned == 16) {
                if (read(aesBlockByte) == -1)
                    return -1;

                aesBytesReturned = 0;
            }

            return aesBlockByte[aesBytesReturned++] & 0xff;
        }

        return read(oneByteBuff, 0, 1) == -1 ? -1 : oneByteBuff[0] & 0xFF;
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

        if ((count < len) && zipModel.isSplitArchive()) {
            in.close();
            in = startNextSplitFile();

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

    private int currSplitFileCounter;

    private DataInput startNextSplitFile() throws IOException {
        Path currSplitFile = zipModel.getZipFile();

        if (currSplitFileCounter != zipModel.getEndCentralDirectory().getSplitParts())
            currSplitFile = ZipModel.getSplitFilePath(currSplitFile, currSplitFileCounter + 1);

        if (!Files.exists(currSplitFile))
            throw new Zip4jException("split file: " + currSplitFile.getFileName() + " does not exists");

        currSplitFileCounter++;
        return new LittleEndianReadFile(currSplitFile);
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
        byte[] mac = in.readBytes(AesEngine.AES_AUTH_LENGTH);

        if (mac.length != AesEngine.AES_AUTH_LENGTH) {
            if (zipModel.isSplitArchive())
                throw new IOException("Error occured while reading stored AES authentication bytes");

            in.close();
            // TODO what if more than one file
            in = startNextSplitFile();
            in.read(mac, mac.length, AesEngine.AES_AUTH_LENGTH - mac.length);
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
