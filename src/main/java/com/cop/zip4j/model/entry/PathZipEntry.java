package com.cop.zip4j.model.entry;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 26.07.2019
 */
@Getter
@RequiredArgsConstructor
public abstract class PathZipEntry extends ZipEntry {

    protected final Path path;
    @Setter
    protected String name;

    @Override
    public boolean isRegularFile() {
        return Files.isRegularFile(path);
    }

    @Override
    public boolean isDirectory() {
        return Files.isDirectory(path);
    }

    @Override
    public String getAbsolutePath() {
        return path.toAbsolutePath().toString();
    }

    public boolean isRoot() {
        return "/".equals(name) || "\\".equals(name);
    }

}
