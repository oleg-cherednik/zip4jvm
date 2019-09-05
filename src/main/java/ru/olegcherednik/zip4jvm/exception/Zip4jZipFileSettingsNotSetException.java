package ru.olegcherednik.zip4jvm.exception;

import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 15.08.2019
 */
public class Zip4jZipFileSettingsNotSetException extends Zip4jException {

    private static final long serialVersionUID = 107063039124498964L;

    public Zip4jZipFileSettingsNotSetException(Path file) {
        super("ZipFileSettings not set for new created zip archive: " + file.toAbsolutePath(), ErrorCode.ZIP_FILE_SETTING_NOT_SET);
    }
}
