package ru.olegcherednik.zip4jvm;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.engine.InfoEngine;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Optional;

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
    private ZipInfoSettings settings = ZipInfoSettings.DEFAULT;

    public static ZipInfo zip(Path zip) {
        requireNotNull(zip, "ZipInfo.zip");
        requireExists(zip);
        requireRegularFile(zip, "ZipInfo.zip");
        return new ZipInfo(zip);
    }

    public ZipInfo settings(ZipInfoSettings settings) {
        this.settings = Optional.ofNullable(settings).orElse(ZipInfoSettings.DEFAULT);
        return this;
    }

    public void printShortInfo(PrintStream out) throws IOException {
        new InfoEngine(zip, settings).printTextInfo(out);
    }

    public void decompose(Path destDir) throws IOException {
        new InfoEngine(zip, settings).decompose(destDir);
    }

}
