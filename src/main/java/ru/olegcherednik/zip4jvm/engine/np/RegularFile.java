package ru.olegcherednik.zip4jvm.engine.np;

import java.nio.file.Path;

final class RegularFile extends NamedPath {

    private final Path file;

    public RegularFile(Path file, String fileName) {
        super(fileName);
        this.file = file;
    }

    @Override
    public Path getPath() {
        return file;
    }

    @Override
    public boolean isRegularFile() {
        return true;
    }

}
