package ru.olegcherednik.zip4jvm.engine;

import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.model.settings.UnzipSettings;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 24.11.2024
 */
@RequiredArgsConstructor
public final class UnzipStreamEngine {

    private final SrcZip srcZip;
    private final UnzipSettings settings;

    public void extract(Path destDir) throws IOException {
        int a = 0;
        a++;
//        for (ZipEntry zipEntry : zipModel.getZipEntries())
//            extractEntry(destDir, zipEntry, ZipEntry::getFileName);
    }


}
