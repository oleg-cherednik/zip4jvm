package ru.olegcherednik.zip4jvm.model;

import lombok.Getter;
import ru.olegcherednik.zip4jvm.utils.PathUtils;

import java.nio.file.Files;
import java.nio.file.Path;

@Getter
public final class NamedPath {

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

    @Override
    public String toString() {
        return String.format("%s [%s]", path, name);
    }

}
