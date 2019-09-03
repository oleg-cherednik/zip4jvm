package com.cop.zip4j;

import com.cop.zip4j.exception.Zip4jException;
import com.cop.zip4j.io.out.DataOutput;
import com.cop.zip4j.io.out.DataOutputStreamDecorator;
import com.cop.zip4j.io.out.SingleZipOutputStream;
import com.cop.zip4j.model.ZipModel;
import com.cop.zip4j.model.builders.ZipModelBuilder;
import com.cop.zip4j.model.entry.PathZipEntry;
import com.cop.zip4j.utils.RemoveEntryFunc;
import com.cop.zip4j.utils.ZipUtils;
import lombok.Builder;
import lombok.NonNull;
import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/**
 * @author Oleg Cherednik
 * @since 15.03.2019
 */
@Builder
public final class ZipMisc {

    @NonNull
    private final Path zipFile;
    @NonNull
    @Builder.Default
    private final Charset charset = StandardCharsets.UTF_8;
    private final char[] password;

    public void clearComment() throws IOException {
        setComment(null);
    }

    public void setComment(String comment) throws IOException {
        comment = ZipUtils.normalizeComment.apply(comment);
        UnzipIt.checkZipFile(zipFile);

        ZipModel zipModel = ZipModelBuilder.readOrCreate(zipFile, charset).noSplitOnly();
        zipModel.setComment(comment);

        try (SingleZipOutputStream out = SingleZipOutputStream.create(zipModel)) {
            out.seek(zipModel.getCentralDirectoryOffs());
        } catch(Exception e) {
            throw new Zip4jException(e);
        }
    }

    public String getComment() throws IOException {
        UnzipIt.checkZipFile(zipFile);
        return ZipModelBuilder.readOrCreate(zipFile, charset).getComment();
    }

    public boolean isEncrypted() throws IOException {
        UnzipIt.checkZipFile(zipFile);
        ZipModel zipModel = ZipModelBuilder.readOrCreate(zipFile, charset);

        return zipModel.getEntries().stream()
                       .anyMatch(PathZipEntry::isEncrypted);
    }

    public List<String> getEntryNames() throws IOException {
        UnzipIt.checkZipFile(zipFile);
        return ZipModelBuilder.readOrCreate(zipFile, charset).getEntryNames();
    }

    public List<Path> getFiles() throws IOException {
        UnzipIt.checkZipFile(zipFile);
        ZipModel zipModel = ZipModelBuilder.readOrCreate(zipFile, charset);

        return LongStream.rangeClosed(0, zipModel.getTotalDisks())
                         .mapToObj(i -> i == 0 ? zipModel.getZipFile() : ZipModel.getSplitFilePath(zipFile, i))
                         .collect(Collectors.toList());
    }

    public boolean isSplit() throws IOException {
        UnzipIt.checkZipFile(zipFile);
        return ZipModelBuilder.readOrCreate(zipFile, charset).isSplit();
    }

    public void removeEntry(@NonNull String entryName) throws IOException {
        removeEntries(Collections.singletonList(entryName));
    }

    public void removeEntries(@NonNull Collection<String> entries) throws IOException {
        UnzipIt.checkZipFile(zipFile);

        ZipModel zipModel = ZipModelBuilder.readOrCreate(zipFile, charset).noSplitOnly();
        new RemoveEntryFunc(zipModel).accept(entries);
    }

    // --------- MergeSplitZip

    public void merge(@NonNull Path destZipFile) throws IOException {
        ZipModel zipModel = ZipModelBuilder.readOrCreate(zipFile, charset);

        // TODO probably if not split archive, just copy single zip file
        if (!zipModel.isSplit())
            throw new Zip4jException("archive not a split zip file");

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

}
