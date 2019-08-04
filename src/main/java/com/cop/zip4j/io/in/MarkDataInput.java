package com.cop.zip4j.io.in;

/**
 * @author Oleg Cherednik
 * @since 04.08.2019
 */
public interface MarkDataInput extends DataInput {

    void mark(String id);

    long getWrittenBytesAmount(String id);

    int getCounter();

}
