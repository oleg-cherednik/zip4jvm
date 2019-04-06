package net.lingala.zip4j;

import lombok.Builder;
import lombok.NonNull;
import net.lingala.zip4j.core.writers.HeaderWriter;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.NoSplitOutputStream;
import net.lingala.zip4j.io.OutputStreamDecorator;
import net.lingala.zip4j.model.CentralDirectory;
import net.lingala.zip4j.model.ZipModel;
import net.lingala.zip4j.util.CreateZipModelSup;
import net.lingala.zip4j.util.RemoveEntryFunc;
import net.lingala.zip4j.util.ZipUtils;
import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
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
    private final Charset charset = Charset.defaultCharset();
    private final char[] password;

    public void clearComment() throws ZipException {
        setComment(null);
    }

    public void setComment(String comment) throws ZipException {
        comment = ZipUtils.normalizeComment(comment);
        UnzipIt.checkZipFile(zipFile);

        ZipModel zipModel = new CreateZipModelSup(zipFile, charset).get().noSplitOnly();
        zipModel.getEndCentralDirectory().setComment(comment);

        try (OutputStreamDecorator out = new OutputStreamDecorator(new NoSplitOutputStream(zipModel.getZipFile()))) {
            out.seek(zipModel.getOffsCentralDirectory());
            new HeaderWriter(zipModel).finalizeZipFileWithoutValidations(out);
        } catch(Exception e) {
            throw new ZipException(e);
        }
    }

    public String getComment() throws ZipException {
        UnzipIt.checkZipFile(zipFile);
        return new CreateZipModelSup(zipFile, charset).get().getEndCentralDirectory().getComment();
    }

    public boolean isEncrypted() {
        UnzipIt.checkZipFile(zipFile);
        ZipModel zipModel = new CreateZipModelSup(zipFile, charset).get();

        return zipModel.getFileHeaders().stream()
                       .anyMatch(CentralDirectory.FileHeader::isEncrypted);
    }

    public List<String> getEntryNames() throws ZipException {
        UnzipIt.checkZipFile(zipFile);
        return new CreateZipModelSup(zipFile, charset).get().getEntryNames();
    }

    public List<Path> getFiles() throws ZipException {
        UnzipIt.checkZipFile(zipFile);
        ZipModel zipModel = new CreateZipModelSup(zipFile, charset).get();

        return IntStream.rangeClosed(0, zipModel.getEndCentralDirectory().getDiskNumber())
                        .mapToObj(i -> i == 0 ? zipModel.getZipFile() : ZipModel.getSplitFilePath(zipFile, i))
                        .collect(Collectors.toList());
    }

    public boolean isSplit() throws ZipException {
        UnzipIt.checkZipFile(zipFile);
        return new CreateZipModelSup(zipFile, charset).get().isSplitArchive();
    }

    public void merge(@NonNull Path destZipFile) {
        ZipModel zipModel = new CreateZipModelSup(zipFile, charset).get();

        // TODO probably if not split archive, just copy single zip file
        if (!zipModel.isSplitArchive())
            throw new ZipException("archive not a split zip file");

        try (OutputStreamDecorator out = new OutputStreamDecorator(new NoSplitOutputStream(destZipFile))) {
            zipModel.convertToSolid(copyAllParts(out.getDelegate(), zipModel));
            new HeaderWriter(zipModel).finalizeZipFileWithoutValidations(out);
        } catch(ZipException e) {
            throw e;
        } catch(Exception e) {
            throw new ZipException(e);
        }
    }

    private static long[] copyAllParts(@NonNull OutputStream out, @NonNull ZipModel zipModel) throws IOException {
        int noOfDisk = zipModel.getEndCentralDirectory().getDiskNumber();
        long[] fileSizeList = new long[noOfDisk + 1];

        for (int i = 0; i <= noOfDisk; i++) {
            try (InputStream in = new FileInputStream(zipModel.getPartFile(i).toFile())) {
                fileSizeList[i] = IOUtils.copyLarge(in, out, 0, i == noOfDisk ? zipModel.getOffsCentralDirectory() : zipModel.getSplitLength());
            }
        }

        return fileSizeList;
    }

    public void removeEntry(@NonNull String entryName) {
        removeEntries(Collections.singletonList(entryName));
    }

    public void removeEntries(@NonNull Collection<String> entries) {
        UnzipIt.checkZipFile(zipFile);

        ZipModel zipModel = new CreateZipModelSup(zipFile, charset).get().noSplitOnly();
        new RemoveEntryFunc(zipModel).accept(entries);
    }

}
