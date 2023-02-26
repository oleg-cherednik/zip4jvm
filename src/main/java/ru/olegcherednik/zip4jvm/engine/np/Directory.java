package ru.olegcherednik.zip4jvm.engine.np;

import ru.olegcherednik.zip4jvm.ZipFile;

import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 24.02.2023
 */
final class Directory extends NamedPath {

    private final Path dir;

    public Directory(Path dir, String dirName) {
        super(dirName);
        this.dir = dir;
    }

    @Override
    public ZipFile.Entry createZipEntry() {
        return ZipFile.Entry.directory(dir, name);
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
