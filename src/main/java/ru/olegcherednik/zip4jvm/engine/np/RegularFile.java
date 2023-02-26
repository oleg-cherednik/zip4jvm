package ru.olegcherednik.zip4jvm.engine.np;

import ru.olegcherednik.zip4jvm.ZipFile;

import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 24.02.2023
 */
final class RegularFile extends NamedPath {

    private final Path file;

    public RegularFile(Path file, String fileName) {
        super(fileName);
        this.file = file;
    }

    @Override
    public ZipFile.Entry createZipEntry() {
        return ZipFile.Entry.of(file, name);
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
