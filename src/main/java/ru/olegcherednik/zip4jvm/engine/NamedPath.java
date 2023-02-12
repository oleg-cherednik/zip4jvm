package ru.olegcherednik.zip4jvm.engine;

import lombok.Getter;
import ru.olegcherednik.zip4jvm.utils.PathUtils;

import java.nio.file.Files;
import java.nio.file.Path;

@Getter
final class NamedPath {

    private final boolean symlink;
    private final Path path;
    private final String name;

    public NamedPath(Path path) {
        this(path, PathUtils.getName(path));
    }

    public NamedPath(Path path, String name) {
        symlink = Files.isSymbolicLink(path);
        this.path = path;
        this.name = name;
    }

    public boolean isExist() {
        return Files.exists(path);
    }

    public boolean isSymlink() {
        return Files.isSymbolicLink(path);
    }

    public boolean isRegularFile() {
        return Files.isRegularFile(path);
    }

    public boolean isDirectory() {
        return Files.isDirectory(path);
    }

    @Override
    public String toString() {
        return String.format("%s [%s]", path, name);
    }

}
