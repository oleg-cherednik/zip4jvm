package com.cop.zip4j.io;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Oleg Cherednik
 * @since 02.08.2019
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class DataOutputStream extends OutputStream {

    public abstract void seek(long pos) throws IOException;

    public abstract long getFilePointer() throws IOException;

    public long getOffs() {
        try {
            return getFilePointer();
        } catch(IOException e) {
            return -1;
        }
    }

    public abstract void writeDword(int val) throws IOException;

    public abstract void writeDword(long val) throws IOException;

    public abstract void writeWord(int val) throws IOException;

    public abstract void writeQword(long val) throws IOException;

    public abstract void writeBytes(byte... buf) throws IOException;

    public abstract void write(byte[] buf, int offs, int len) throws IOException;

    public abstract int getCurrSplitFileCounter();

    @Override
    public String toString() {
        return "offs: " + getOffs();
    }

    @Override
    public void write(int b) throws IOException {
        writeBytes((byte)b);
    }
}
