package com.cop.zip4j.model.entry;

import com.cop.zip4j.exception.ZipException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 26.07.2019
 */
public abstract class ZipEntry {

    // TODO should be ZipEntry
    public static PathZipEntry of(Path path) {
        if (Files.isDirectory(path))
            return new DirectoryZipEntry(path);
        if (Files.isRegularFile(path))
            return new RegularFileZipEntry(path);
        throw new ZipException("Cannot add neither directory nor regular file to zip");
    }

    public abstract String getAbsolutePath();

    public boolean isRegularFile() {
        return false;
    }

    public long size() throws IOException {
        return 0;
    }

    public long crc32() throws IOException {
        return 0;
    }

}
