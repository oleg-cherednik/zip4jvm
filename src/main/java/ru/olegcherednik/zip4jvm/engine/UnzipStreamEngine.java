package ru.olegcherednik.zip4jvm.engine;

import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.io.in.file.consecutive.SolidConsecutiveAccessDataInput;
import ru.olegcherednik.zip4jvm.io.in.file.consecutive.SplitConsecutiveAccessDataInput;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.builders.ZipModelBuilder;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.model.password.PasswordProvider;
import ru.olegcherednik.zip4jvm.model.settings.UnzipSettings;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * This engine does not use random access to the zip file and it does not read {@link CentralDirectory} at all.
 *
 * @author Oleg Cherednik
 * @since 24.11.2024
 */
public final class UnzipStreamEngine extends BaseUnzipEngine {

    private final ZipModel zipModel;

    public static UnzipStreamEngine create(SrcZip srcZip, UnzipSettings settings) {
        PasswordProvider passwordProvider = settings.getPasswordProvider();
        Function<Charset, Charset> charsetCustomizer = settings.getCharsetCustomizer();
        ZipModel zipModel = ZipModelBuilder.readAlt(srcZip, charsetCustomizer, passwordProvider);
        return new UnzipStreamEngine(zipModel, settings.getPasswordProvider());
    }

    public UnzipStreamEngine(ZipModel zipModel, PasswordProvider passwordProvider) {
        super(passwordProvider);
        this.zipModel = zipModel;
    }

    public void extract(Path dstDir) throws IOException {
        try (DataInput in = createDataInput(zipModel.getSrcZip())) {
            Iterator<ZipEntry> it = zipModel.offsAscIterator();

            while (it.hasNext()) {
                ZipEntry zipEntry = it.next();
                in.seekForward(zipEntry.getLocalFileHeaderAbsOffs());
                extractEntry1(dstDir, zipEntry, in, ZipEntry::getFileName);
            }
        }
    }

    void extract(Path dstDir, Map<String, Function<ZipEntry, String>> map) throws IOException {
        try (DataInput in = createDataInput(zipModel.getSrcZip())) {
            Iterator<ZipEntry> it = zipModel.offsAscIterator();

            while (it.hasNext()) {
                ZipEntry zipEntry = it.next();

                if (map.containsKey(zipEntry.getFileName())) {
                    in.seekForward(zipEntry.getLocalFileHeaderAbsOffs());
                    extractEntry1(dstDir, zipEntry, in, map.get(zipEntry.getFileName()));
                }
            }
        }
    }

    public static DataInput createDataInput(SrcZip srcZip) throws IOException {
        return srcZip.isSolid() ? new SolidConsecutiveAccessDataInput(srcZip)
                                : new SplitConsecutiveAccessDataInput(srcZip);
    }

}
