package ru.olegcherednik.zip4jvm.engine.np;

import java.nio.file.Path;

final class Directory extends NamedPath {

    private final Path dir;

    public Directory(Path dir, String dirName) {
        super(dirName);
        this.dir = dir;
    }

    @Override
    public Path getPath() {
        return dir;
    }

    @Override
    public boolean isDirectory() {
        return true;
    }

}
