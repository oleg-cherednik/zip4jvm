package ru.olegcherednik.zip4jvm.exception;

import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 11.08.2019
 */
public class Zip4jvmPathNotExistsException extends Zip4jvmException {

    private static final long serialVersionUID = 6634130368683535775L;

    public Zip4jvmPathNotExistsException(Path path) {
        super("Path not exists: " + path, ErrorCode.PATH_NOT_EXISTS);
    }
}
