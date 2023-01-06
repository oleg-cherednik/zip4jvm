package ru.olegcherednik.zip4jvm.decompose;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 05.01.2023
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class EmptyDecompose implements Decompose {

    public static final EmptyDecompose INSTANCE = new EmptyDecompose();

    @Override
    public boolean printTextInfo(PrintStream out, boolean emptyLine) {
        return false;
    }

    @Override
    public Path decompose(Path dir) throws IOException {
        return null;
    }
}
