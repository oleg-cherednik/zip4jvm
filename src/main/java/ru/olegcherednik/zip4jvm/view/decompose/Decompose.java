package ru.olegcherednik.zip4jvm.view.decompose;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 12.12.2019
 */
public interface Decompose {

    boolean printTextInfo(PrintStream out, boolean emptyLine);

    void decompose(Path dir) throws IOException;

}
