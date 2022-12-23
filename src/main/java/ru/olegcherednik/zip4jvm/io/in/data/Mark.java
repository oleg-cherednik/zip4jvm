package ru.olegcherednik.zip4jvm.io.in.data;

/**
 * @author Oleg Cherednik
 * @since 20.12.2022
 */
public interface Mark {

    void mark(String id);

    long getMark(String id);

    long getMarkSize(String id);

}
