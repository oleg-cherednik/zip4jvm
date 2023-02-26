package ru.olegcherednik.zip4jvm.engine.np;

import ru.olegcherednik.zip4jvm.ZipFile;
import ru.olegcherednik.zip4jvm.model.ExternalFileAttributes;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntryBuilder;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.utils.ZipUtils;

import java.nio.file.Files;
import java.nio.file.Path;

import static ru.olegcherednik.zip4jvm.model.ExternalFileAttributes.PROP_OS_NAME;

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
    public ZipFile.Entry createZipFileEntry() {
        return ZipFile.Entry.directory(dir, name);
    }

//    @Override
//    public ZipEntry createZipEntry(ZipEntrySettings entrySettings) {
//        return ZipUtils.readQuietly(() -> {
//            long lastModifiedTime = Files.getLastModifiedTime(dir).toMillis();
//            ExternalFileAttributes externalFileAttributes = ExternalFileAttributes.build(PROP_OS_NAME)
//                                                                                  .readFrom(dir)
//                                                                                  .directory();
//            return ZipEntryBuilder.emptyDirectoryEntry(name, lastModifiedTime, externalFileAttributes);
//        });
//    }

    @Override
    public Path getPath() {
        return dir;
    }

    @Override
    public boolean isDirectory() {
        return true;
    }

}
