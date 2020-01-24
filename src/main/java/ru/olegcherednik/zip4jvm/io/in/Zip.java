package ru.olegcherednik.zip4jvm.io.in;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 20.01.2020
 */
public abstract class Zip {

    public abstract Path getPath();

    public abstract Path getDiskPath(int disk);

    public abstract long getTotalDisks();

    public abstract long length() throws IOException;

    public abstract DataInputFile openDataInputFile() throws IOException;

}
