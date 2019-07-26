package com.cop.zip4j.model.entry;

import lombok.Getter;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 26.07.2019
 */
@Getter
public class RegularFileZipEntry extends PathZipEntry {

    public RegularFileZipEntry(Path file) {
        super(file);
    }

    @Override
    public long size() throws IOException {
        return Files.size(path);
    }

    @Override
    public long crc32() throws IOException {
        return FileUtils.checksumCRC32(path.toFile());
    }

}
