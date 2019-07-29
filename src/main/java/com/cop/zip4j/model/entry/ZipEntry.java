package com.cop.zip4j.model.entry;

import com.cop.zip4j.exception.Zip4jException;
import lombok.NonNull;
import org.apache.commons.io.FileUtils;

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
        if (Files.isRegularFile(path)) {
            try {
                long size = Files.size(path);
                long crc32 = FileUtils.checksumCRC32(path.toFile());
                return new RegularFileZipEntry(path, size, crc32);
            } catch(IOException e) {
                throw new Zip4jException(e);
            }
        }
        throw new Zip4jException("Cannot add neither directory nor regular file to zip");
    }

    public abstract String getAbsolutePath();

    public boolean isRegularFile() {
        return false;
    }

    public boolean isDirectory() {
        return false;
    }

    public long size() {
        return 0;
    }

    public long crc32() {
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
