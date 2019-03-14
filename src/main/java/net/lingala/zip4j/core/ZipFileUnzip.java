package net.lingala.zip4j.core;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.CentralDirectory;
import net.lingala.zip4j.model.UnzipParameters;
import net.lingala.zip4j.model.ZipModel;
import net.lingala.zip4j.unzip.Unzip;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 14.03.2019
 */
@Setter
@Getter
@RequiredArgsConstructor
public final class ZipFileUnzip {

    @NonNull
    private final Path zipFile;
    @NonNull
    private Charset charset = Charset.defaultCharset();
    private ZipModel zipModel;

    public void extract(@NonNull Path destDir, @NonNull UnzipParameters parameters) throws ZipException {
        checkZipFile();
        checkOutputFolder(destDir);

        zipModel = ZipFile.createZipModel(zipFile, charset);
        new Unzip(zipModel).extract(destDir, parameters);
    }

    /**
     * Extracts a specific path from the zip path to the destination path.
     * If destination path is invalid, then this method throws an exception.
     * <br><br>
     * If newFileName is not null or empty, newly created path name will be replaced by
     * the value in newFileName. If this value is null, then the path name will be the
     * value in FileHeader.getFileName
     *
     * @param fileHeader
     * @param destPath
     * @param unzipParameters
     * @throws ZipException
     */
    public void extractFile(CentralDirectory.FileHeader fileHeader, Path destPath, UnzipParameters unzipParameters) throws ZipException {
        extractFile(fileHeader, destPath, unzipParameters, null);
    }

    /**
     * Extracts a specific path from the zip path to the destination path.
     * If destination path is invalid, then this method throws an exception.
     *
     * @param fileHeader
     * @param destDir
     * @param unzipParameters
     * @param newFileName
     * @throws ZipException
     */
    public void extractFile(@NonNull CentralDirectory.FileHeader fileHeader, Path destDir,
            UnzipParameters unzipParameters, String newFileName) throws ZipException {
        zipModel = ZipFile.createZipModel(zipFile, charset);
        internalExtractFile(fileHeader, destDir, unzipParameters, newFileName);

    }

    /**
     * Extracts a specific path from the zip path to the destination path.
     * This method first finds the necessary path header from the input path name.
     * <br><br>
     * File name is relative path name in the zip path. For example if a zip path contains
     * a path "a.txt", then to extract this path, input path name has to be "a.txt". Another
     * example is if there is a path "b.txt" in a folder "abc" in the zip path, then the
     * input path name has to be abc/b.txt
     * <br><br>
     * Throws an exception if path header could not be found for the given path name or if
     * the destination path is invalid
     *
     * @param fileName
     * @param destPath
     * @throws ZipException
     */
    public void extractFile(String fileName, Path destDir) throws ZipException {
        extractFile(fileName, destDir, null);
    }

    /**
     * Extracts a specific path from the zip path to the destination path.
     * This method first finds the necessary path header from the input path name.
     * <br><br>
     * File name is relative path name in the zip path. For example if a zip path contains
     * a path "a.txt", then to extract this path, input path name has to be "a.txt". Another
     * example is if there is a path "b.txt" in a folder "abc" in the zip path, then the
     * input path name has to be abc/b.txt
     * <br><br>
     * Throws an exception if path header could not be found for the given path name or if
     * the destination path is invalid
     *
     * @param fileName
     * @param destPath
     * @param unzipParameters
     * @throws ZipException
     */
    public void extractFile(String fileName,
            Path destDir, UnzipParameters unzipParameters) throws ZipException {
        extractFile(fileName, destDir, unzipParameters, null);
    }

    /**
     * Extracts a specific path from the zip path to the destination path.
     * This method first finds the necessary path header from the input path name.
     * <br><br>
     * File name is relative path name in the zip path. For example if a zip path contains
     * a path "a.txt", then to extract this path, input path name has to be "a.txt". Another
     * example is if there is a path "b.txt" in a folder "abc" in the zip path, then the
     * input path name has to be abc/b.txt
     * <br><br>
     * If newFileName is not null or empty, newly created path name will be replaced by
     * the value in newFileName. If this value is null, then the path name will be the
     * value in FileHeader.getFileName
     * <br><br>
     * Throws an exception if path header could not be found for the given path name or if
     * the destination path is invalid
     *
     * @param fileName
     * @param destDir
     * @param unzipParameters
     * @param newFileName
     * @throws ZipException
     */
    public void extractFile(String fileName, Path destDir,
            UnzipParameters unzipParameters, String newFileName) throws ZipException {
        zipModel = ZipFile.createZipModel(zipFile, charset);

        CentralDirectory.FileHeader fileHeader = zipModel.getFileHeader(fileName);

        if (fileHeader == null) {
            throw new ZipException("path header not found for given path name, cannot extract path");
        }

        internalExtractFile(fileHeader, destDir, unzipParameters, newFileName);
    }

    /**
     * Extracts file to the specified directory using any
     * user defined parameters in UnzipParameters. Output file name
     * will be overwritten with the value in newFileName. If this
     * parameter is null, then file name will be the same as in
     * FileHeader.getFileName
     */
    private void internalExtractFile(CentralDirectory.FileHeader fileHeader, Path destDir,
            UnzipParameters unzipParameters, String newFileName) throws ZipException {
        if (zipModel == null) {
            throw new ZipException("input zipModel is null");
        }

        checkOutputFolder(destDir);

        new Unzip(zipModel).extractFile(fileHeader, destDir, unzipParameters, newFileName);
    }

    private void checkZipFile() throws ZipException {
        if (!Files.isRegularFile(zipFile))
            throw new ZipException("ZipFile is not a regular file: " + zipFile);
        if (!Files.exists(zipFile))
            throw new ZipException("ZipFile not exists: " + zipFile);
    }

    private static void checkOutputFolder(@NonNull Path dir) throws ZipException {

//        if(!Files.isDirectory(dir))
//            throw new ZipException("Destination path is not a directory: " + dir);
//
//        if (Files.exists(dir)) {
//
//            if (!file.isDirectory()) {
//                throw new ZipException("output folder is not valid");
//            }
//
//            if (!file.canWrite()) {
//                throw new ZipException("no write access to output folder");
//            }
//        } else {
//            try {
//                file.mkdirs();
//                if (!file.isDirectory()) {
//                    throw new ZipException("output folder is not valid");
//                }
//
//                if (!file.canWrite()) {
//                    throw new ZipException("no write access to destination folder");
//                }
//
////				SecurityManager manager = new SecurityManager();
////				try {
////					manager.checkWrite(file.getAbsolutePath());
////				} catch (Exception e) {
////					e.printStackTrace();
////					throw new ZipException("no write access to destination folder");
////				}
//            } catch(Exception e) {
//                throw new ZipException("Cannot create destination folder");
//            }
//        }
    }
}
