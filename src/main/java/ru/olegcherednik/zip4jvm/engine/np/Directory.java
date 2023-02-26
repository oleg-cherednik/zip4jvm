package ru.olegcherednik.zip4jvm.engine.np;

import ru.olegcherednik.zip4jvm.ZipFile;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntryBuilder;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;

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

        assert dirName.endsWith("/") : dirName;
    }

    @Override
    public ZipFile.Entry createZipFileEntry() {
        return ZipFile.Entry.directory(dir, name);
    }

    @Override
    public ZipEntry createZipEntry(ZipEntrySettings entrySettings) {
        return ZipEntryBuilder.emptyDirectory(dir, name, entrySettings);
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
