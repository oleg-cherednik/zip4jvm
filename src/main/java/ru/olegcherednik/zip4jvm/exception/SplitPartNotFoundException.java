package ru.olegcherednik.zip4jvm.exception;

import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 25.08.2020
 */
public class SplitPartNotFoundException extends Zip4jvmException {

    public SplitPartNotFoundException(Path file) {
        super(String.format("Split part '%s' was not found", file));
    }

}
