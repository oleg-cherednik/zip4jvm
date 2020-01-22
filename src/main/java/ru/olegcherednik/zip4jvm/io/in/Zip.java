package ru.olegcherednik.zip4jvm.io.in;

import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 20.01.2020
 */
public interface Zip {

    Path getPath();

    Path getDiskPath();

    long getTotalDisks();

}
