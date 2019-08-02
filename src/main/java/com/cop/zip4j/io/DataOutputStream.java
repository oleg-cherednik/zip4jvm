package com.cop.zip4j.io;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 02.08.2019
 */
public interface DataOutputStream {

    void mark(String id);

    long getWrittenBytesAmount(String id);

    long getFilePointer() throws IOException;

    void writeDword(int val) throws IOException;

    void writeDword(long val) throws IOException;

    void writeWord(int val) throws IOException;

    void writeQword(long val) throws IOException;

    void writeBytes(byte... buf) throws IOException;

    void writeBytes(byte[] buf, int offs, int len) throws IOException;

    int getCurrSplitFileCounter();

}
