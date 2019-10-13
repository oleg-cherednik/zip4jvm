package ru.olegcherednik.zip4jvm.io;

/**
 * @author Oleg Cherednik
 * @since 12.10.2019
 */
public interface Marker {

    void mark(String id);

    long getMark(String id);

    long getWrittenBytesAmount(String id);

}
