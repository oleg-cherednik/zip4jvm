package ru.olegcherednik.zip4jvm;

import lombok.Builder;
import lombok.NonNull;
import org.apache.commons.io.IOUtils;
import ru.olegcherednik.zip4jvm.exception.Zip4jException;
import ru.olegcherednik.zip4jvm.io.out.DataOutput;
import ru.olegcherednik.zip4jvm.io.out.DataOutputStreamDecorator;
import ru.olegcherednik.zip4jvm.io.out.SingleZipOutputStream;
import ru.olegcherednik.zip4jvm.io.readers.ZipModelReader;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.builders.ZipModelBuilder;
import ru.olegcherednik.zip4jvm.utils.RemoveEntryFunc;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * @author Oleg Cherednik
 * @since 15.03.2019
 */
@Builder
public final class ZipMisc {

    @NonNull
    private final Path zipFile;
    private final char[] password;

    public static void setComment(@NonNull Path zip, String comment) throws IOException {
        try (ZipFile.Misc zipFile = ZipFile.misc(zip)) {
            zipFile.setComment(comment);
        }
    }

    public static String getComment(@NonNull Path zip) throws IOException {
        return ZipFile.read(zip).getComment();
    }

    public Set<String> getEntryNames() throws IOException {
        checkZipFile(zipFile);
        return new ZipModelReader(zipFile).read().getEntryNames();
    }

    public boolean isSplit() throws IOException {
        checkZipFile(zipFile);
        return new ZipModelReader(zipFile).read().isSplit();
    }

    public void removeEntry(@NonNull String entryName) throws IOException {
        removeEntries(Collections.singletonList(entryName));
    }

    public void removeEntries(@NonNull Collection<String> entries) throws IOException {
        checkZipFile(zipFile);
        ZipModel zipModel = new ZipModelReader(zipFile).read().noSplitOnly();
        new RemoveEntryFunc(zipModel).accept(entries);
    }

    // --------- MergeSplitZip

    public void merge(@NonNull Path destZipFile) throws IOException {
        ZipModel zipModel = ZipModelBuilder.readOrCreate(zipFile).noSplitOnly();

        try {
            Files.createDirectories(destZipFile.getParent());
        } catch(IOException e) {
            throw new Zip4jException(e);
        }

        try (DataOutput out = SingleZipOutputStream.create(destZipFile, zipModel)) {
            convertToSolid(copyAllParts(new DataOutputStreamDecorator(out), zipModel), zipModel);
        } catch(Zip4jException e) {
            throw e;
        } catch(Exception e) {
            throw new Zip4jException(e);
        }
    }

    private static void convertToSolid(long[] fileSizeList, ZipModel zipModel) {
        long offs = Arrays.stream(fileSizeList).sum();

        zipModel.getEntries().forEach(entry -> {
            // TODO it doesn't work in ZIP64 where disk is long
            entry.setLocalFileHeaderOffs(entry.getLocalFileHeaderOffs() + Arrays.stream(fileSizeList, 0, (int)entry.getDisk()).sum());
            entry.setDisk(0);
        });

        zipModel.setSplitSize(ZipModel.NO_SPLIT);
        zipModel.setCentralDirectoryOffs(zipModel.getCentralDirectoryOffs() + offs);
    }

    private static long[] copyAllParts(@NonNull OutputStream out, @NonNull ZipModel zipModel) throws IOException {
        // TODO it doesn't work in ZIP64 where totalDisks is long
        int totalDisks = (int)zipModel.getTotalDisks();
        long[] fileSizeList = new long[totalDisks + 1];

        for (int i = 0; i <= totalDisks; i++) {
            try (InputStream in = new FileInputStream(zipModel.getPartFile(i).toFile())) {
                fileSizeList[i] = IOUtils.copyLarge(in, out, 0,
                        i == totalDisks ? zipModel.getCentralDirectoryOffs() : zipModel.getSplitSize());
            }
        }

        return fileSizeList;
    }

    private static void checkZipFile(Path zipFile) {
        if (!Files.exists(zipFile))
            throw new Zip4jException("ZipFile not exists: " + zipFile);
        if (!Files.isRegularFile(zipFile))
            throw new Zip4jException("ZipFile is not a regular file: " + zipFile);
    }

}
