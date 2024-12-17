package ru.olegcherednik.zip4jvm.engine;

import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.io.in.file.consecutive.SolidSequentialAccessDataInput;
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
import java.util.function.Function;

/**
 * This engine does not use random access to the zip file and it does not read {@link CentralDirectory} at all.
 *
 * @author Oleg Cherednik
 * @since 24.11.2024
 */
public final class UnzipStreamEngine extends BaseUnzipEngine {

    private final SrcZip srcZip;
    private final ZipModel zipModel;

    public static UnzipStreamEngine create(SrcZip srcZip, UnzipSettings settings) {
        PasswordProvider passwordProvider = settings.getPasswordProvider();
        Function<Charset, Charset> charsetCustomizer = settings.getCharsetCustomizer();
        ZipModel zipModel = ZipModelBuilder.read(srcZip, charsetCustomizer, passwordProvider);
        return new UnzipStreamEngine(srcZip, zipModel, settings.getPasswordProvider());
    }

    public UnzipStreamEngine(SrcZip srcZip, ZipModel zipModel, PasswordProvider passwordProvider) {
        super(passwordProvider);
        this.srcZip = srcZip;
        this.zipModel = zipModel;
    }

    public void extract(Path destDir) throws IOException {
        try (DataInput in = new SolidSequentialAccessDataInput(srcZip)) {
            Iterator<ZipEntry> it = zipModel.offsAscIterator();

            while (it.hasNext()) {
                ZipEntry zipEntry = it.next();
                in.seekForward(srcZip.getAbsOffs(zipEntry.getDiskNo(), zipEntry.getLocalFileHeaderRelativeOffs()));
                extractEntry1(destDir, zipEntry, in, ZipEntry::getFileName);
                int a = 0;
                a++;
            }
        }
    }

//    public static DataInput createDataInput(SrcZip srcZip) throws IOException {
//        return srcZip.isSolid() ? new SolidSequentialAccessDataInput(srcZip)
//                                : new SplitRandomAccessDataInputFile(srcZip);
//    }

}
