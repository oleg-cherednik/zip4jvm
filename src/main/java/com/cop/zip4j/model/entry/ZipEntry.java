package com.cop.zip4j.model.entry;

import com.cop.zip4j.exception.ZipException;
import lombok.NonNull;

import java.io.IOException;
import java.io.OutputStream;
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

    public boolean isDirectory() {
        return false;
    }

    public long size() throws IOException {
        return 0;
    }

    public long crc32() throws IOException {
        return 0;
    }

    public long write(@NonNull OutputStream out) throws IOException {
        return 0;
    }

    @Override
    public String toString() {
        return getAbsolutePath();
    }

}
