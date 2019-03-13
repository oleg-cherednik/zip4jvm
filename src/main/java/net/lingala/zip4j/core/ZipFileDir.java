package net.lingala.zip4j.core;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipModel;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.zip.ZipEngine;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 13.03.2019
 */
@Setter
@Getter
@RequiredArgsConstructor
public final class ZipFileDir {

    @NonNull
    private final Path path;
    @NonNull
    private Charset charset = Charset.defaultCharset();

    private ZipModel zipModel;

    /**
     * Creates a zip path and adds the files/folders from the specified folder to the zip path.
     * This method does the same functionality as in addFolder method except that this method
     * can also create split zip files when adding a folder. To create a split zip path, set the
     * splitArchive parameter to true and specify the splitLength. Split length has to be more than
     * or equal to 65536 bytes. Note that this method throws an exception if the zip path already
     * exists.
     *
     * @param folderToAdd
     * @param parameters
     * @param splitLength
     * @throws ZipException
     */
    public void createZipFileFromFolder(String folderToAdd, ZipParameters parameters, long splitLength) throws ZipException, IOException {

        if (StringUtils.isBlank(folderToAdd)) {
            throw new ZipException("folderToAdd is empty or null, cannot create Zip File from folder");
        }

        createZipFileFromFolder(new File(folderToAdd), parameters, splitLength);

    }

    /**
     * Creates a zip path and adds the files/folders from the specified folder to the zip path.
     * This method does the same functionality as in addFolder method except that this method
     * can also create split zip files when adding a folder. To create a split zip path, set the
     * splitArchive parameter to true and specify the splitLength. Split length has to be more than
     * or equal to 65536 bytes. Note that this method throws an exception if the zip path already
     * exists.
     *
     * @param folderToAdd
     * @param parameters
     * @param splitLength
     * @throws ZipException
     */
    public void createZipFileFromFolder(File folderToAdd, ZipParameters parameters, long splitLength) throws ZipException, IOException {

        if (folderToAdd == null)
            throw new ZipException("folderToAdd is null, cannot create zip path from folder");
        if (parameters == null)
            throw new ZipException("input parameters are null, cannot create zip path from folder");
        if (Files.exists(path))
            throw new ZipException("zip path: " + path + " already exists. To add files to existing zip path use addFolder method");

        zipModel = ZipFile.createZipModel(path, charset);
        zipModel.setSplitLength(splitLength);

        addFolder(folderToAdd, parameters, false);
    }

    /**
     * Adds the folder in the given path to the zip path. If zip path does not exist,
     * then a new zip path is created. If input folder path is invalid then an exception
     * is thrown. Zip parameters for the files in the folder to be added can be set in
     * the input parameters
     *
     * @param path
     * @param parameters
     * @throws ZipException
     */
    public void addFolder(String path, ZipParameters parameters) throws ZipException, IOException {
        if (StringUtils.isBlank(path)) {
            throw new ZipException("input path is null or empty, cannot add folder to zip path");
        }

        addFolder(new File(path), parameters);
    }

    /**
     * Adds the folder in the given path object to the zip path. If zip path does not exist,
     * then a new zip path is created. If input folder is invalid then an exception
     * is thrown. Zip parameters for the files in the folder to be added can be set in
     * the input parameters
     *
     * @param path
     * @param parameters
     * @throws ZipException
     */
    public void addFolder(File path, ZipParameters parameters) throws ZipException, IOException {
        if (path == null) {
            throw new ZipException("input path is null, cannot add folder to zip path");
        }

        if (parameters == null) {
            throw new ZipException("input parameters are null, cannot add folder to zip path");
        }

        addFolder(path, parameters, true);
    }

    /**
     * Internal method to add a folder to the zip path.
     *
     * @param path
     * @param parameters
     * @param checkSplitArchive
     * @throws ZipException
     */
    private void addFolder(File path, ZipParameters parameters, boolean checkSplitArchive) throws ZipException, IOException {
        zipModel = ZipFile.readOrCreateModel(zipModel, this.path, charset);

        if (this.zipModel == null) {
            throw new ZipException("internal error: zip model is null");
        }

        if (checkSplitArchive) {
            if (this.zipModel.isSplitArchive()) {
                throw new ZipException("This is a split archive. Zip path format does not allow updating split/spanned files");
            }
        }

        new ZipEngine(zipModel).addFolderToZip(path, parameters);

    }
}
