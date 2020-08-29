package ru.olegcherednik.zip4jvm.model.builders;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;
import ru.olegcherednik.zip4jvm.io.readers.ZipModelReader;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.Charsets;
import ru.olegcherednik.zip4jvm.model.EndCentralDirectory;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntryBuilder;
import ru.olegcherednik.zip4jvm.model.settings.ZipSettings;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 01.09.2019
 */
@RequiredArgsConstructor
public final class ZipModelBuilder {

    private final SrcZip srcZip;
    private final EndCentralDirectory endCentralDirectory;
    private final Zip64 zip64;
    private final CentralDirectory centralDirectory;
    private final Function<Charset, Charset> charsetCustomizer;

    public static ZipModel read(SrcZip srcZip) throws IOException {
        return read(srcZip, Charsets.UNMODIFIED);
    }

    public static ZipModel read(SrcZip srcZip, Function<Charset, Charset> charsetCustomizer) throws IOException {
        return new ZipModelReader(srcZip, charsetCustomizer).read();
    }

    public static ZipModel build(Path zip, ZipSettings settings) {
        if (Files.exists(zip))
            throw new Zip4jvmException("ZipFile '" + zip.toAbsolutePath() + "' exists");

        ZipModel zipModel = new ZipModel(SrcZip.of(zip));
        zipModel.setSplitSize(settings.getSplitSize());
        zipModel.setComment(settings.getComment());
        zipModel.setZip64(settings.isZip64());

        return zipModel;
    }

    public ZipModel build() throws IOException {
        ZipModel zipModel = new ZipModel(srcZip);

        zipModel.setZip64(zip64 != Zip64.NULL);
        zipModel.setComment(endCentralDirectory.getComment());
        zipModel.setTotalDisks(getTotalDisks());
        zipModel.setMainDisk(getMainDisks());
        zipModel.setCentralDirectorySize(getCentralDirectorySize());
        zipModel.setCentralDirectoryRelativeOffs(getCentralDirectoryRelativeOffs(endCentralDirectory, zip64));
        createAndAddEntries(zipModel);
        updateSplit(zipModel);

        return zipModel;
    }

    private void createAndAddEntries(ZipModel zipModel) {
        centralDirectory.getFileHeaders().stream()
                        .map(fileHeader -> ZipEntryBuilder.build(fileHeader, zipModel, charsetCustomizer))
                        .forEach(zipModel::addEntry);
    }

    private int getTotalDisks() {
        if (zip64 == Zip64.NULL)
            return endCentralDirectory.getTotalDisks();
        return (int)zip64.getEndCentralDirectory().getTotalDisks();
    }

    private long getMainDisks() {
        if (zip64 == Zip64.NULL)
            return endCentralDirectory.getMainDiskNo();
        return zip64.getEndCentralDirectoryLocator().getMainDisk();
    }

    public long getCentralDirectorySize() {
        if (zip64 == Zip64.NULL)
            return endCentralDirectory.getCentralDirectorySize();
        return zip64.getEndCentralDirectory().getCentralDirectorySize();
    }

    public static int getMainDiskNo(EndCentralDirectory endCentralDirectory, Zip64 zip64) {
        if (zip64 == Zip64.NULL)
            return endCentralDirectory.getMainDiskNo();
        return (int)zip64.getEndCentralDirectory().getMainDiskNo();
    }

    public static long getCentralDirectoryRelativeOffs(EndCentralDirectory endCentralDirectory, Zip64 zip64) {
        if (zip64 == Zip64.NULL)
            return endCentralDirectory.getCentralDirectoryRelativeOffs();
        return zip64.getEndCentralDirectory().getCentralDirectoryRelativeOffs();
    }

    public static long getTotalEntries(EndCentralDirectory endCentralDirectory, Zip64 zip64) {
        if (zip64 == Zip64.NULL)
            return endCentralDirectory.getTotalEntries();
        return zip64.getEndCentralDirectory().getTotalEntries();
    }

    private static void updateSplit(ZipModel zipModel) throws IOException {
        if (zipModel.getSrcZip().isSplit())
            zipModel.setSplitSize(getSplitSize(zipModel));
    }

    private static long getSplitSize(ZipModel zipModel) throws IOException {
        long size = 0;

        for (int diskNo = 0; diskNo <= zipModel.getTotalDisks(); diskNo++)
            size = Math.max(size, Files.size(zipModel.getDiskFile(diskNo)));

        return size;
    }

}
