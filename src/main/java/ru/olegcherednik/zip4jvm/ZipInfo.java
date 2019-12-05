package ru.olegcherednik.zip4jvm;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.engine.DecomposeEngine;
import ru.olegcherednik.zip4jvm.model.Charsets;
import ru.olegcherednik.zip4jvm.model.settings.DecomposeSettings;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireExists;
import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireNotNull;
import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireRegularFile;

/**
 * @author Oleg Cherednik
 * @since 11.10.2019
 */
@RequiredArgsConstructor
public final class ZipInfo {

    private final Path zip;

    public static ZipInfo zip(Path zip) {
        requireNotNull(zip, "ZipInfo.zip");
        requireExists(zip);
        requireRegularFile(zip, "ZipInfo.zip");
        return new ZipInfo(zip);
    }

    public void getShortInfo(PrintStream out) throws IOException {
        DecomposeSettings settings = DecomposeSettings.builder()
                                                      .customizeCharset(charset -> Charsets.UTF_8)
                                                      .offs(4)
                                                      .columnWidth(52).build();
        new DecomposeEngine(zip, settings).getShortInfo(out);
    }

    public void decompose(Path destDir) throws IOException {
        DecomposeSettings settings = DecomposeSettings.builder()
                                                      .customizeCharset(charset -> Charsets.UTF_8)
                                                      .offs(4)
                                                      .columnWidth(52).build();
        new DecomposeEngine(zip, settings).decompose(destDir);
    }

}
