package net.lingala.zip4j;

import lombok.Builder;
import lombok.NonNull;
import net.lingala.zip4j.core.HeaderWriter;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.NoSplitOutputStream;
import net.lingala.zip4j.io.SplitOutputStream;
import net.lingala.zip4j.model.Encryption;
import net.lingala.zip4j.model.Zip64EndCentralDirectory;
import net.lingala.zip4j.model.Zip64EndCentralDirectoryLocator;
import net.lingala.zip4j.model.ZipModel;
import net.lingala.zip4j.util.ArchiveMaintainer;
import net.lingala.zip4j.util.InternalZipConstants;
import net.lingala.zip4j.util.Raw;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
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

        if (zipModel == null) {
            ZipException e = new ZipException("one of the input parameters is null, cannot merge split zip file");
            throw e;
        }

        if (!zipModel.isSplitArchive()) {
            ZipException e = new ZipException("archive not a split zip file");
            throw e;
        }

        OutputStream outputStream = null;
        RandomAccessFile inputStream = null;
        ArrayList fileSizeList = new ArrayList();
        long totBytesWritten = 0;
        boolean splitSigRemoved = false;
        try {

            int totNoOfSplitFiles = zipModel.getEndCentralDirectory().getNoOfDisk();

            if (totNoOfSplitFiles <= 0) {
                throw new ZipException("corrupt zip model, archive not a split zip file");
            }

            outputStream = prepareOutputStreamForMerge(destZipFile.toFile());
            for (int i = 0; i <= totNoOfSplitFiles; i++) {
                inputStream = createSplitZipFileHandler(zipModel, i);

                int start = 0;
                Long end = new Long(inputStream.length());

                if (i == 0) {
                    if (!zipModel.isEmpty()) {
                        byte[] buff = new byte[4];
                        inputStream.seek(0);
                        inputStream.read(buff);
                        if (Raw.readIntLittleEndian(buff, 0) == InternalZipConstants.SPLITSIG) {
                            start = 4;
                            splitSigRemoved = true;
                        }
                    }
                }

                if (i == totNoOfSplitFiles) {
                    end = new Long(zipModel.getEndCentralDirectory().getOffOfStartOfCentralDir());
                }

                ArchiveMaintainer.copyFile(inputStream, outputStream, start, end.longValue());
                totBytesWritten += (end.longValue() - start);

                fileSizeList.add(end);

                try {
                    inputStream.close();
                } catch(IOException e) {
                    //ignore
                }
            }

            ZipModel newZipModel = (ZipModel)zipModel.clone();
            newZipModel.getEndCentralDirectory().setOffOfStartOfCentralDir(totBytesWritten);

            updateSplitZipModel(newZipModel, fileSizeList, splitSigRemoved);

            HeaderWriter headerWriter = new HeaderWriter();
            headerWriter.finalizeZipFileWithoutValidations(newZipModel, outputStream);

        } catch(Exception e) {
            throw new ZipException(e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch(IOException e) {
                    //ignore
                }
            }

            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch(IOException e) {
                    // ignore
                }
            }
        }
    }

    private OutputStream prepareOutputStreamForMerge(File outFile) throws ZipException {
        if (outFile == null) {
            throw new ZipException("outFile is null, cannot create outputstream");
        }

        try {
            return new FileOutputStream(outFile);
        } catch(FileNotFoundException e) {
            throw new ZipException(e);
        } catch(Exception e) {
            throw new ZipException(e);
        }
    }

    private RandomAccessFile createSplitZipFileHandler(ZipModel zipModel, int partNumber) throws ZipException {
        if (zipModel == null) {
            throw new ZipException("zip model is null, cannot create split file handler");
        }

        if (partNumber < 0) {
            throw new ZipException("invlaid part number, cannot create split file handler");
        }

        try {
            String curZipFile = zipModel.getZipFile().toString();
            String partFile = null;

            if (partNumber == zipModel.getEndCentralDirectory().getNoOfDisk())
                partFile = zipModel.getZipFile().toString();
            else
                partFile = ZipModel.getSplitFilePath(zipModel.getZipFile(), partNumber + 1).toString();

            File tmpFile = new File(partFile);

            if (!tmpFile.exists()) {
                throw new ZipException("split file does not exist: " + partFile);
            }

            return new RandomAccessFile(tmpFile, InternalZipConstants.READ_MODE);
        } catch(FileNotFoundException e) {
            throw new ZipException(e);
        } catch(Exception e) {
            throw new ZipException(e);
        }

    }

    public static void updateSplitZipModel(ZipModel zipModel, ArrayList fileSizeList, boolean splitSigRemoved) throws ZipException {
        if (zipModel == null)
            throw new ZipException("zip model is null, cannot update split zip model");

        zipModel.setNoSplitArchive();
        updateSplitFileHeader(zipModel, fileSizeList, splitSigRemoved);
        updateSplitEndCentralDirectory(zipModel);
        if (zipModel.isZip64Format()) {
            updateSplitZip64EndCentralDirLocator(zipModel, fileSizeList);
            updateSplitZip64EndCentralDirRec(zipModel, fileSizeList);
        }
    }

    private static void updateSplitFileHeader(ZipModel zipModel, ArrayList fileSizeList, boolean splitSigRemoved) throws ZipException {
        try {
            int fileHeaderCount = zipModel.getCentralDirectory().getFileHeaders().size();
            int splitSigOverhead = 0;
            if (splitSigRemoved)
                splitSigOverhead = 4;

            for (int i = 0; i < fileHeaderCount; i++) {
                long offsetLHToAdd = 0;

                for (int j = 0; j < zipModel.getCentralDirectory().getFileHeaders().get(i).getDiskNumberStart(); j++) {
                    offsetLHToAdd += (Long)fileSizeList.get(j);
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

    private static void updateSplitZip64EndCentralDirLocator(ZipModel zipModel, ArrayList fileSizeList) throws ZipException {
        if (zipModel == null) {
            throw new ZipException("zip model is null, cannot update split Zip64 end of central directory locator");
        }

        if (zipModel.getZip64EndCentralDirectoryLocator() == null) {
            return;
        }

        zipModel.getZip64EndCentralDirectoryLocator().setNoOfDiskStartOfZip64EndOfCentralDirRec(0);
        long offsetZip64EndCentralDirRec = 0;

        for (int i = 0; i < fileSizeList.size(); i++) {
            offsetZip64EndCentralDirRec += ((Long)fileSizeList.get(i)).longValue();
        }
        zipModel.getZip64EndCentralDirectoryLocator().setOffsetZip64EndOfCentralDirRec(
                ((Zip64EndCentralDirectoryLocator)zipModel.getZip64EndCentralDirectoryLocator()).getOffsetZip64EndOfCentralDirRec() +
                        offsetZip64EndCentralDirRec);
        zipModel.getZip64EndCentralDirectoryLocator().setTotNumberOfDiscs(1);
    }

    private static void updateSplitZip64EndCentralDirRec(ZipModel zipModel, ArrayList fileSizeList) throws ZipException {
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

        for (int i = 0; i < fileSizeList.size(); i++) {
            offsetStartCenDirWRTStartDiskNo += ((Long)fileSizeList.get(i)).longValue();
        }

        zipModel.getZip64EndCentralDirectory().setOffsetStartCenDirWRTStartDiskNo(
                ((Zip64EndCentralDirectory)zipModel.getZip64EndCentralDirectory()).getOffsetStartCenDirWRTStartDiskNo() +
                        offsetStartCenDirWRTStartDiskNo);
    }


}
