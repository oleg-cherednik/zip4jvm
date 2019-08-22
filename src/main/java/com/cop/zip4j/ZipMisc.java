package com.cop.zip4j;

import com.cop.zip4j.exception.Zip4jException;
import com.cop.zip4j.io.out.DataOutput;
import com.cop.zip4j.io.out.DataOutputStreamDecorator;
import com.cop.zip4j.io.out.SingleZipOutputStream;
import com.cop.zip4j.io.writers.ZipModelWriter;
import com.cop.zip4j.model.CentralDirectory;
import com.cop.zip4j.model.ZipModel;
import com.cop.zip4j.utils.CreateZipModel;
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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    public void clearComment() throws Zip4jException {
        setComment(null);
    }

    public void setComment(String comment) throws Zip4jException {
        comment = ZipUtils.normalizeComment.apply(comment);
        UnzipIt.checkZipFile(zipFile);

        ZipModel zipModel = new CreateZipModel(zipFile, charset).get().noSplitOnly();
        zipModel.getEndCentralDirectory().setComment(comment);

        try (SingleZipOutputStream out = SingleZipOutputStream.create(zipModel)) {
            out.seek(zipModel.getCentralDirectoryOffs());
            new ZipModelWriter(zipModel, false).finalizeZipFile(out);
        } catch(Exception e) {
            throw new Zip4jException(e);
        }
    }

    public String getComment() throws Zip4jException {
        UnzipIt.checkZipFile(zipFile);
        return new CreateZipModel(zipFile, charset).get().getEndCentralDirectory().getComment();
    }

    public boolean isEncrypted() {
        UnzipIt.checkZipFile(zipFile);
        ZipModel zipModel = new CreateZipModel(zipFile, charset).get();

        return zipModel.getFileHeaders().stream()
                       .anyMatch(CentralDirectory.FileHeader::isEncrypted);
    }

    public List<String> getEntryNames() throws Zip4jException {
        UnzipIt.checkZipFile(zipFile);
        return new CreateZipModel(zipFile, charset).get().getEntryNames();
    }

    public List<Path> getFiles() throws Zip4jException {
        UnzipIt.checkZipFile(zipFile);
        ZipModel zipModel = new CreateZipModel(zipFile, charset).get();

        return IntStream.rangeClosed(0, zipModel.getEndCentralDirectory().getSplitParts())
                        .mapToObj(i -> i == 0 ? zipModel.getZipFile() : ZipModel.getSplitFilePath(zipFile, i))
                        .collect(Collectors.toList());
    }

    public boolean isSplit() throws Zip4jException {
        UnzipIt.checkZipFile(zipFile);
        return new CreateZipModel(zipFile, charset).get().isSplitArchive();
    }

    public void merge(@NonNull Path dstZipFile) {
        ZipModel zipModel = new CreateZipModel(zipFile, charset).get();

        // TODO probably if not split archive, just copy single zip file
        if (!zipModel.isSplitArchive())
            throw new Zip4jException("archive not a split zip file");

        try {
            Files.createDirectories(dstZipFile.getParent());
        } catch(IOException e) {
            throw new Zip4jException(e);
        }

        try (DataOutput out = SingleZipOutputStream.create(dstZipFile, zipModel)) {
            zipModel.convertToSolid(copyAllParts(new DataOutputStreamDecorator(out), zipModel));
            new ZipModelWriter(zipModel, false).finalizeZipFile(out);
        } catch(Zip4jException e) {
            throw e;
        } catch(Exception e) {
            throw new Zip4jException(e);
        }
    }

    private static long[] copyAllParts(@NonNull OutputStream out, @NonNull ZipModel zipModel) throws IOException {
        int noOfDisk = zipModel.getEndCentralDirectory().getSplitParts();
        long[] fileSizeList = new long[noOfDisk + 1];

        for (int i = 0; i <= noOfDisk; i++) {
            try (InputStream in = new FileInputStream(zipModel.getPartFile(i + 1).toFile())) {
                fileSizeList[i] = IOUtils.copyLarge(in, out, 0, i == noOfDisk ? zipModel.getCentralDirectoryOffs() : zipModel.getSplitLength());
            }
        }

        return fileSizeList;
    }

    public void removeEntry(@NonNull String entryName) {
        removeEntries(Collections.singletonList(entryName));
    }

    public void removeEntries(@NonNull Collection<String> entries) {
        UnzipIt.checkZipFile(zipFile);

        ZipModel zipModel = new CreateZipModel(zipFile, charset).get().noSplitOnly();
        new RemoveEntryFunc(zipModel).accept(entries);
    }

}
