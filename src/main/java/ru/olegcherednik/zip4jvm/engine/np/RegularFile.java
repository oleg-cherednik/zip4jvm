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
final class RegularFile extends NamedPath {

    private final Path file;

    public RegularFile(Path file, String fileName) {
        super(fileName);
        this.file = file;
    }

    @Override
    public ZipFile.Entry createZipFileEntry() {
        return ZipFile.Entry.regularFile(file, name);
    }

    @Override
    public ZipEntry createZipEntry(ZipEntrySettings entrySettings) {
        return ZipEntryBuilder.regularFile(file, name, entrySettings);
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
