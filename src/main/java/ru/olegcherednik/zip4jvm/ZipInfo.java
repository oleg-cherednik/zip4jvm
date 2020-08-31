package ru.olegcherednik.zip4jvm;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Optional;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireNotNull;

/**
 * @author Oleg Cherednik
 * @since 11.10.2019
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class ZipInfo {

    private final SrcZip srcZip;
    private ZipInfoSettings settings = ZipInfoSettings.DEFAULT;

    public static ZipInfo zip(Path zip) {
        requireNotNull(zip, "ZipInfo.zip");
        return new ZipInfo(SrcZip.of(zip));
    }

    public ZipInfo settings(ZipInfoSettings settings) {
        this.settings = Optional.ofNullable(settings).orElse(ZipInfoSettings.DEFAULT);
        return this;
    }

    public void printShortInfo() throws IOException {
        printShortInfo(System.out);
    }

    public void printShortInfo(PrintStream out) throws IOException {
        ZipFile.info(srcZip, settings).printTextInfo(out);
    }

    public void decompose(Path destDir) throws IOException {
        ZipFile.info(srcZip, settings).decompose(destDir);
    }

    public CentralDirectory.FileHeader getFileHeader(String entryName) throws IOException {
        return ZipFile.info(srcZip, settings).getFileHeader(entryName);
    }

}
