package net.lingala.zip4j;

import lombok.Builder;
import lombok.NonNull;
import net.lingala.zip4j.core.HeaderWriter;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.NoSplitOutputStream;
import net.lingala.zip4j.io.SplitOutputStream;
import net.lingala.zip4j.model.CentralDirectory;
import net.lingala.zip4j.model.Encryption;
import net.lingala.zip4j.model.EndCentralDirectory;
import net.lingala.zip4j.model.Zip64EndCentralDirectory;
import net.lingala.zip4j.model.Zip64EndCentralDirectoryLocator;
import net.lingala.zip4j.model.ZipModel;
import net.lingala.zip4j.util.InternalZipConstants;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Arrays;
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
        comment = StringUtils.isEmpty(comment) ? null : comment.trim();
        UnzipIt.checkZipFile(zipFile);

        ZipModel zipModel = ZipFile.createZipModel(zipFile, charset);
        ZipIt.checkSplitArchiveModification(zipModel);

        if (StringUtils.length(comment) > InternalZipConstants.MAX_ALLOWED_ZIP_COMMENT_LENGTH)
            throw new ZipException("comment length exceeds maximum length");

        zipModel.getEndCentralDirectory().setComment(comment);

        try (SplitOutputStream out = new NoSplitOutputStream(zipModel.getZipFile())) {
            out.seek(zipModel.getOffOfStartOfCentralDir());
            new HeaderWriter().finalizeZipFileWithoutValidations(zipModel, out);
        } catch(Exception e) {
            throw new ZipException(e);
        }
    }

    /**
     * Returns the comment set for the Zip path
     *
     * @return String
     * @throws ZipException
     */
    public String getComment() throws ZipException {
        UnzipIt.checkZipFile(zipFile);
        return ZipFile.createZipModel(zipFile, charset).getEndCentralDirectory().getComment();
    }

    public boolean isEncrypted() throws ZipException {
        UnzipIt.checkZipFile(zipFile);
        ZipModel zipModel = ZipFile.createZipModel(zipFile, charset);

        return zipModel.getCentralDirectory().getFileHeaders().stream()
                       .anyMatch(fileHeader -> fileHeader.getEncryption() != Encryption.OFF);
    }

    public List<String> getEntryNames() throws ZipException {
        UnzipIt.checkZipFile(zipFile);
        return ZipFile.createZipModel(zipFile, charset).getEntryNames();
    }

    public List<Path> getFiles() throws ZipException {
        UnzipIt.checkZipFile(zipFile);
        ZipModel zipModel = ZipFile.createZipModel(zipFile, charset);

        return IntStream.rangeClosed(0, zipModel.getEndCentralDirectory().getNoOfDisk())
                        .mapToObj(i -> i == 0 ? zipModel.getZipFile() : ZipModel.getSplitFilePath(zipFile, i))
                        .collect(Collectors.toList());
    }

    public boolean isSplit() throws ZipException {
        UnzipIt.checkZipFile(zipFile);
        return ZipFile.createZipModel(zipFile, charset).isSplitArchive();
    }

    public void merge(@NonNull Path destZipFile) throws ZipException {
        ZipModel zipModel = ZipFile.createZipModel(zipFile, charset);

        if (!zipModel.isSplitArchive())
            throw new ZipException("archive not a split zip file");

        try (OutputStream out = new FileOutputStream(destZipFile.toFile())) {
            long[] fileSizeList = new long[zipModel.getEndCentralDirectory().getNoOfDisk() + 1];
            long totalBytesWritten = copyAllParts(out, zipModel, fileSizeList);
            // TODO what about big file? look at zipModel.getOffOfStartOfCentralDir()
            zipModel.getEndCentralDirectory().setOffOfStartOfCentralDir(totalBytesWritten);
            updateZipModel(zipModel, fileSizeList);
            new HeaderWriter().finalizeZipFileWithoutValidations(zipModel, out);
        } catch(ZipException e) {
            throw e;
        } catch(Exception e) {
            throw new ZipException(e);
        }
    }

    @SuppressWarnings("MethodCanBeVariableArityMethod")
    private static long copyAllParts(@NonNull OutputStream out, @NonNull ZipModel zipModel, @NonNull long[] fileSizeList) throws IOException {
        int noOfDisk = zipModel.getEndCentralDirectory().getNoOfDisk();
        long totalSize = 0;

        for (int i = 0; i <= noOfDisk; i++) {
            try (InputStream in = new FileInputStream(zipModel.getPartFile(i).toFile())) {
                long length = IOUtils.copyLarge(in, out, 0, i == noOfDisk ? zipModel.getOffOfStartOfCentralDir() : zipModel.getSplitLength());
                totalSize += length;
                fileSizeList[i] = length;
            }
        }

        return totalSize;
    }

    @SuppressWarnings("MethodCanBeVariableArityMethod")
    private static void updateZipModel(ZipModel zipModel, long[] fileSizeList) throws ZipException {
        long totalBytesWritten = Arrays.stream(fileSizeList).sum();

        zipModel.setNoSplitArchive();
        updateFileHeaders(zipModel, fileSizeList);
        updateEndCentralDirectory(zipModel);
        updateZip64EndCentralDirLocator(zipModel, totalBytesWritten);
        updateZip64EndCentralDirRec(zipModel, totalBytesWritten);
    }

    @SuppressWarnings("MethodCanBeVariableArityMethod")
    private static void updateFileHeaders(ZipModel zipModel, long[] fileSizeList) {
        zipModel.getCentralDirectory().getFileHeaders().forEach(fileHeader -> {
            fileHeader.updateOffLocalHeaderRelative(Arrays.stream(fileSizeList, 0, fileHeader.getDiskNumberStart()).sum());
            fileHeader.setDiskNumberStart(0);
        });
    }

    private static void updateEndCentralDirectory(ZipModel zipModel) throws ZipException {
        CentralDirectory centralDirectory = zipModel.getCentralDirectory();
        EndCentralDirectory endCentralDirectory = zipModel.getEndCentralDirectory();

        endCentralDirectory.setNoOfDisk(0);
        endCentralDirectory.setNoOfDiskStartCentralDir(0);
        endCentralDirectory.setTotNoOfEntriesInCentralDir(centralDirectory.getFileHeaders().size());
        endCentralDirectory.setTotalNumberOfEntriesInCentralDirOnThisDisk(centralDirectory.getFileHeaders().size());
    }

    private static void updateZip64EndCentralDirLocator(ZipModel zipModel, long totalBytesWritten) throws ZipException {
        if (!zipModel.isZip64Format())
            return;

        Zip64EndCentralDirectoryLocator locator = zipModel.getZip64EndCentralDirectoryLocator();

        locator.setNoOfDiskStartOfZip64EndOfCentralDirRec(0);
        locator.updateOffsetZip64EndOfCentralDirRec(totalBytesWritten);
        locator.setTotNumberOfDiscs(1);
    }

    private static void updateZip64EndCentralDirRec(ZipModel zipModel, long totalBytesWritten) throws ZipException {
        if (!zipModel.isZip64Format())
            return;

        Zip64EndCentralDirectory dir = zipModel.getZip64EndCentralDirectory();

        dir.setNoOfThisDisk(0);
        dir.setNoOfThisDiskStartOfCentralDir(0);
        dir.setTotNoOfEntriesInCentralDirOnThisDisk(zipModel.getEndCentralDirectory().getTotNoOfEntriesInCentralDir());
        dir.updateOffsetStartCenDirWRTStartDiskNo(totalBytesWritten);
    }
}
