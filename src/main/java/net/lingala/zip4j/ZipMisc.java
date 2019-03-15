package net.lingala.zip4j;

import lombok.Builder;
import lombok.NonNull;
import net.lingala.zip4j.core.HeaderWriter;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.NoSplitOutputStream;
import net.lingala.zip4j.io.SplitOutputStream;
import net.lingala.zip4j.model.Encryption;
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
import java.util.ArrayList;
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
            List<Long> fileSizeList = new ArrayList<>();
            long totalBytesWritten = copyAllParts(out, zipModel, fileSizeList);
            // TODO what about big file? look at zipModel.getOffOfStartOfCentralDir()
            zipModel.getEndCentralDirectory().setOffOfStartOfCentralDir(totalBytesWritten);
            updateSplitZipModel(zipModel, fileSizeList);
            new HeaderWriter().finalizeZipFileWithoutValidations(zipModel, out);
        } catch(ZipException e) {
            throw e;
        } catch(Exception e) {
            throw new ZipException(e);
        }
    }

    private static long copyAllParts(@NonNull OutputStream out, @NonNull ZipModel zipModel, @NonNull List<Long> fileSizeList) throws IOException {
        int noOfDisk = zipModel.getEndCentralDirectory().getNoOfDisk();
        long totalSize = 0;

        for (int i = 0; i <= noOfDisk; i++) {
            try (InputStream in = new FileInputStream(zipModel.getPartFile(i).toFile())) {
                long length = IOUtils.copyLarge(in, out, 0, i == noOfDisk ? zipModel.getOffOfStartOfCentralDir() : zipModel.getSplitLength());
                totalSize += length;
                fileSizeList.add(length);
            }
        }

        return totalSize;
    }

    public static void updateSplitZipModel(ZipModel zipModel, List<Long> fileSizeList) throws ZipException {
        if (zipModel == null)
            throw new ZipException("zip model is null, cannot update split zip model");

        zipModel.setNoSplitArchive();
        updateSplitFileHeader(zipModel, fileSizeList);
        updateSplitEndCentralDirectory(zipModel);
        if (zipModel.isZip64Format()) {
            updateSplitZip64EndCentralDirLocator(zipModel, fileSizeList);
            updateSplitZip64EndCentralDirRec(zipModel, fileSizeList);
        }
    }

    private static void updateSplitFileHeader(ZipModel zipModel, List<Long> fileSizeList) throws ZipException {
        try {
            int fileHeaderCount = zipModel.getCentralDirectory().getFileHeaders().size();
            int splitSigOverhead = 0;

            for (int i = 0; i < fileHeaderCount; i++) {
                long offsetLHToAdd = 0;

                for (int j = 0; j < zipModel.getCentralDirectory().getFileHeaders().get(i).getDiskNumberStart(); j++) {
                    offsetLHToAdd += fileSizeList.get(j);
                }
                zipModel.getCentralDirectory().getFileHeaders().get(i).setOffLocalHeaderRelative(
                        zipModel.getCentralDirectory().getFileHeaders().get(i).getOffLocalHeaderRelative() +
                                offsetLHToAdd - splitSigOverhead);
                zipModel.getCentralDirectory().getFileHeaders().get(i).setDiskNumberStart(0);
            }

        } catch(Exception e) {
            throw new ZipException(e);
        }
    }

    private static void updateSplitEndCentralDirectory(ZipModel zipModel) throws ZipException {
        try {
            if (zipModel == null)
                throw new ZipException("zip model is null - cannot update end of central directory for split zip model");

            zipModel.getEndCentralDirectory().setNoOfDisk(0);
            zipModel.getEndCentralDirectory().setNoOfDiskStartCentralDir(0);
            zipModel.getEndCentralDirectory().setTotNoOfEntriesInCentralDir(
                    zipModel.getCentralDirectory().getFileHeaders().size());
            zipModel.getEndCentralDirectory().setTotalNumberOfEntriesInCentralDirOnThisDisk(
                    zipModel.getCentralDirectory().getFileHeaders().size());

        } catch(ZipException e) {
            throw e;
        } catch(Exception e) {
            throw new ZipException(e);
        }
    }

    private static void updateSplitZip64EndCentralDirLocator(ZipModel zipModel, List<Long> fileSizeList) throws ZipException {
        if (zipModel == null) {
            throw new ZipException("zip model is null, cannot update split Zip64 end of central directory locator");
        }

        if (zipModel.getZip64EndCentralDirectoryLocator() == null) {
            return;
        }

        zipModel.getZip64EndCentralDirectoryLocator().setNoOfDiskStartOfZip64EndOfCentralDirRec(0);
        long offsetZip64EndCentralDirRec = 0;

        for (int i = 0; i < fileSizeList.size(); i++) {
            offsetZip64EndCentralDirRec += fileSizeList.get(i);
        }
        zipModel.getZip64EndCentralDirectoryLocator().setOffsetZip64EndOfCentralDirRec(
                zipModel.getZip64EndCentralDirectoryLocator().getOffsetZip64EndOfCentralDirRec() +
                        offsetZip64EndCentralDirRec);
        zipModel.getZip64EndCentralDirectoryLocator().setTotNumberOfDiscs(1);
    }

    private static void updateSplitZip64EndCentralDirRec(ZipModel zipModel, List<Long> fileSizeList) throws ZipException {
        if (zipModel == null) {
            throw new ZipException("zip model is null, cannot update split Zip64 end of central directory record");
        }

        if (zipModel.getZip64EndCentralDirectory() == null) {
            return;
        }

        zipModel.getZip64EndCentralDirectory().setNoOfThisDisk(0);
        zipModel.getZip64EndCentralDirectory().setNoOfThisDiskStartOfCentralDir(0);
        zipModel.getZip64EndCentralDirectory().setTotNoOfEntriesInCentralDirOnThisDisk(
                zipModel.getEndCentralDirectory().getTotNoOfEntriesInCentralDir());

        long offsetStartCenDirWRTStartDiskNo = 0;

        for (int i = 0; i < fileSizeList.size(); i++)
            offsetStartCenDirWRTStartDiskNo += fileSizeList.get(i);

        zipModel.getZip64EndCentralDirectory().setOffsetStartCenDirWRTStartDiskNo(
                zipModel.getZip64EndCentralDirectory().getOffsetStartCenDirWRTStartDiskNo() +
                        offsetStartCenDirWRTStartDiskNo);
    }


}
