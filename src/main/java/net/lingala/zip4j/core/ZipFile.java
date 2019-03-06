/*
 * Copyright 2010 Srikanth Reddy Lingala
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this path except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.lingala.zip4j.core;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.lingala.zip4j.core.readers.ZipModelReader;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.ZipInputStream;
import net.lingala.zip4j.model.CentralDirectory;
import net.lingala.zip4j.model.UnzipParameters;
import net.lingala.zip4j.model.ZipModel;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.progress.ProgressMonitor;
import net.lingala.zip4j.unzip.Unzip;
import net.lingala.zip4j.util.ArchiveMaintainer;
import net.lingala.zip4j.util.Zip4jUtil;
import net.lingala.zip4j.zip.ZipEngine;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class to handle zip files. Some of the operations supported
 * in this class are:<br>
 * <ul>
 * <li>Create Zip File</li>
 * <li>Add files to zip path</li>
 * <li>Add folder to zip path</li>
 * <li>Extract files from zip files</li>
 * <li>Remove files from zip path</li>
 * </ul>
 */

@Getter
@Setter
@RequiredArgsConstructor
public class ZipFile {

    private final Path path;

    private ZipModel zipModel;
    private boolean isEncrypted;
    private final ProgressMonitor progressMonitor = new ProgressMonitor();
    private boolean runInThread;
    @NonNull
    private Charset charset = Charset.defaultCharset();

    /**
     * Creates a zip path and adds the source path to the zip path. If the zip path
     * exists then this method throws an exception. Parameters such as compression type, etc
     * can be set in the input parameters
     *
     * @param sourceFile - File to be added to the zip path
     * @param parameters - parameters to create the zip path
     * @throws ZipException
     */
    public void createZipFile(File sourceFile, ZipParameters parameters) throws ZipException {
        List<File> sourceFileList = new ArrayList<>();
        sourceFileList.add(sourceFile);
        createZipFile(sourceFileList, parameters, false, -1);
    }

    /**
     * Creates a zip path and adds the source path to the zip path. If the zip path
     * exists then this method throws an exception. Parameters such as compression type, etc
     * can be set in the input parameters. While the method addFile/addFiles also creates the
     * zip path if it does not exist, the main functionality of this method is to create a split
     * zip path. To create a split zip path, set the splitArchive parameter to true with a valid
     * splitLength. Split Length has to be more than 65536 bytes
     *
     * @param sourceFile   - File to be added to the zip path
     * @param parameters   - parameters to create the zip path
     * @param splitArchive - if archive has to be split or not
     * @param splitLength  - if archive has to be split, then length in bytes at which it has to be split
     * @throws ZipException
     */
    public void createZipFile(File sourceFile, ZipParameters parameters,
            boolean splitArchive, long splitLength) throws ZipException {

        List<File> sourceFileList = new ArrayList<>();
        sourceFileList.add(sourceFile);
        createZipFile(sourceFileList, parameters, splitArchive, splitLength);
    }

    /**
     * Creates a zip path and adds the list of source path(s) to the zip path. If the zip path
     * exists then this method throws an exception. Parameters such as compression type, etc
     * can be set in the input parameters
     *
     * @param sourceFileList - File to be added to the zip path
     * @param parameters     - parameters to create the zip path
     * @throws ZipException
     */
    public void createZipFile(List<File> sourceFileList,
            ZipParameters parameters) throws ZipException {
        createZipFile(sourceFileList, parameters, false, -1);
    }

    /**
     * Creates a zip path and adds the list of source path(s) to the zip path. If the zip path
     * exists then this method throws an exception. Parameters such as compression type, etc
     * can be set in the input parameters. While the method addFile/addFiles also creates the
     * zip path if it does not exist, the main functionality of this method is to create a split
     * zip path. To create a split zip path, set the splitArchive parameter to true with a valid
     * splitLength. Split Length has to be more than 65536 bytes
     *
     * @param sourceFileList - File to be added to the zip path
     * @param parameters     - zip parameters for this path list
     * @param splitArchive   - if archive has to be split or not
     * @param splitLength    - if archive has to be split, then length in bytes at which it has to be split
     * @throws ZipException
     */
    public void createZipFile(List<File> sourceFileList, ZipParameters parameters,
            boolean splitArchive, long splitLength) throws ZipException {
        if (Files.exists(path))
            throw new ZipException("zip path: " + path + " already exists. To add files to existing zip path use addFile method");

        if (sourceFileList == null)
            throw new ZipException("input path ArrayList is null, cannot create zip path");

        zipModel = createZipModel();
        this.zipModel.setSplitArchive(splitArchive);
        this.zipModel.setSplitLength(splitLength);
        addFiles(sourceFileList, parameters);
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
     * @param splitArchive
     * @param splitLength
     * @throws ZipException
     */
    public void createZipFileFromFolder(String folderToAdd, ZipParameters parameters,
            boolean splitArchive, long splitLength) throws ZipException {

        if (!Zip4jUtil.isStringNotNullAndNotEmpty(folderToAdd)) {
            throw new ZipException("folderToAdd is empty or null, cannot create Zip File from folder");
        }

        createZipFileFromFolder(new File(folderToAdd), parameters, splitArchive, splitLength);

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
     * @param splitArchive
     * @param splitLength
     * @throws ZipException
     */
    public void createZipFileFromFolder(File folderToAdd, ZipParameters parameters,
            boolean splitArchive, long splitLength) throws ZipException {

        if (folderToAdd == null)
            throw new ZipException("folderToAdd is null, cannot create zip path from folder");
        if (parameters == null)
            throw new ZipException("input parameters are null, cannot create zip path from folder");
        if (Files.exists(path))
            throw new ZipException("zip path: " + path + " already exists. To add files to existing zip path use addFolder method");

        zipModel = createZipModel();
        this.zipModel.setSplitArchive(splitArchive);
        if (splitArchive)
            this.zipModel.setSplitLength(splitLength);

        addFolder(folderToAdd, parameters, false);
    }

    /**
     * Adds input source path to the zip path. If zip path does not exist, then
     * this method creates a new zip path. Parameters such as compression type, etc
     * can be set in the input parameters.
     *
     * @param sourceFile - File to tbe added to the zip path
     * @param parameters - zip parameters for this path
     * @throws ZipException
     */
    public void addFile(File sourceFile, ZipParameters parameters) throws ZipException {
        List<File> sourceFileList = new ArrayList<>();
        sourceFileList.add(sourceFile);
        addFiles(sourceFileList, parameters);
    }

    /**
     * Adds the list of input files to the zip path. If zip path does not exist, then
     * this method creates a new zip path. Parameters such as compression type, etc
     * can be set in the input parameters.
     *
     * @param sourceFileList
     * @param parameters
     * @throws ZipException
     */
    public void addFiles(List<File> sourceFileList, ZipParameters parameters) throws ZipException {

        checkZipModel();

        if (this.zipModel == null) {
            throw new ZipException("internal error: zip model is null");
        }

        if (sourceFileList == null) {
            throw new ZipException("input path ArrayList is null, cannot add files");
        }

        if (parameters == null) {
            throw new ZipException("input parameters are null, cannot add files to zip");
        }

        if (progressMonitor.getState() == ProgressMonitor.STATE_BUSY) {
            throw new ZipException("invalid operation - Zip4j is in busy state");
        }

        if (Files.exists(path) && zipModel.isSplitArchive())
            throw new ZipException("Zip path already exists. Zip path format does not allow updating split/spanned files");

        ZipEngine zipEngine = new ZipEngine(zipModel);
        zipEngine.addFiles(sourceFileList, parameters, progressMonitor, runInThread);
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
    public void addFolder(String path, ZipParameters parameters) throws ZipException {
        if (!Zip4jUtil.isStringNotNullAndNotEmpty(path)) {
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
    public void addFolder(File path, ZipParameters parameters) throws ZipException {
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
    private void addFolder(File path, ZipParameters parameters,
            boolean checkSplitArchive) throws ZipException {

        checkZipModel();

        if (this.zipModel == null) {
            throw new ZipException("internal error: zip model is null");
        }

        if (checkSplitArchive) {
            if (this.zipModel.isSplitArchive()) {
                throw new ZipException("This is a split archive. Zip path format does not allow updating split/spanned files");
            }
        }

        ZipEngine zipEngine = new ZipEngine(zipModel);
        zipEngine.addFolderToZip(path, parameters, progressMonitor, runInThread);

    }

    /**
     * Creates a new entry in the zip path and adds the content of the inputstream to the
     * zip path. ZipParameters.isSourceExternalStream and ZipParameters.fileNameInZip have to be
     * set before in the input parameters. If the path name ends with / or \, this method treats the
     * content as a directory. Setting the flag ProgressMonitor.setRunInThread to true will have
     * no effect for this method and hence this method cannot be used to add content to zip in
     * thread mode
     *
     * @param inputStream
     * @param parameters
     * @throws ZipException
     */
    public void addStream(InputStream inputStream, ZipParameters parameters) throws ZipException {
        if (inputStream == null) {
            throw new ZipException("inputstream is null, cannot add path to zip");
        }

        if (parameters == null) {
            throw new ZipException("zip parameters are null");
        }

        this.setRunInThread(false);

        checkZipModel();

        if (this.zipModel == null) {
            throw new ZipException("internal error: zip model is null");
        }

        if (Files.exists(path) && zipModel.isSplitArchive())
            throw new ZipException("Zip path already exists. Zip path format does not allow updating split/spanned files");

        ZipEngine zipEngine = new ZipEngine(zipModel);
        zipEngine.addStreamToZip(inputStream, parameters);
    }

    /**
     * Reads the zip header information for this zip path. If the zip path
     * does not exist, then this method throws an exception.<br><br>
     * <b>Note:</b> This method does not read local path header information
     *
     * @throws ZipException
     */
    private ZipModel readZipModel() throws ZipException {
        return zipModel == null ? new ZipModelReader(path, charset).read() : zipModel;
    }


    /**
     * Extracts all the files in the given zip path to the input destination path.
     * If zip path does not exist or destination path is invalid then an
     * exception is thrown.
     *
     * @param destPath
     * @throws ZipException
     */
    public void extractAll(String destPath) throws ZipException {
        extractAll(destPath, null);

    }

    /**
     * Extracts all the files in the given zip path to the input destination path.
     * If zip path does not exist or destination path is invalid then an
     * exception is thrown.
     *
     * @param destPath
     * @param unzipParameters
     * @throws ZipException
     */
    public void extractAll(String destPath,
            UnzipParameters unzipParameters) throws ZipException {

        if (!Zip4jUtil.isStringNotNullAndNotEmpty(destPath)) {
            throw new ZipException("output path is null or invalid");
        }

        if (!Zip4jUtil.checkOutputFolder(destPath)) {
            throw new ZipException("invalid output path");
        }

        zipModel = readZipModel();

        // Throw an exception if zipModel is still null
        if (zipModel == null) {
            throw new ZipException("Internal error occurred when extracting zip path");
        }

        if (progressMonitor.getState() == ProgressMonitor.STATE_BUSY) {
            throw new ZipException("invalid operation - Zip4j is in busy state");
        }

        Unzip unzip = new Unzip(zipModel);
        unzip.extractAll(unzipParameters, destPath, progressMonitor, runInThread);

    }

    /**
     * Extracts a specific path from the zip path to the destination path.
     * If destination path is invalid, then this method throws an exception.
     *
     * @param fileHeader
     * @param destPath
     * @throws ZipException
     */
    public void extractFile(CentralDirectory.FileHeader fileHeader, String destPath) throws ZipException {
        extractFile(fileHeader, destPath, null);
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
    public void extractFile(CentralDirectory.FileHeader fileHeader,
            String destPath, UnzipParameters unzipParameters) throws ZipException {
        extractFile(fileHeader, destPath, unzipParameters, null);
    }

    /**
     * Extracts a specific path from the zip path to the destination path.
     * If destination path is invalid, then this method throws an exception.
     *
     * @param fileHeader
     * @param destPath
     * @param unzipParameters
     * @param newFileName
     * @throws ZipException
     */
    public void extractFile(CentralDirectory.FileHeader fileHeader, String destPath,
            UnzipParameters unzipParameters, String newFileName) throws ZipException {

        if (fileHeader == null) {
            throw new ZipException("input path header is null, cannot extract path");
        }

        if (!Zip4jUtil.isStringNotNullAndNotEmpty(destPath)) {
            throw new ZipException("destination path is empty or null, cannot extract path");
        }

        zipModel = readZipModel();

        if (progressMonitor.getState() == ProgressMonitor.STATE_BUSY) {
            throw new ZipException("invalid operation - Zip4j is in busy state");
        }

        fileHeader.extractFile(zipModel, destPath, unzipParameters, newFileName, progressMonitor, runInThread);

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
    public void extractFile(String fileName, String destPath) throws ZipException {
        extractFile(fileName, destPath, null);
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
            String destPath, UnzipParameters unzipParameters) throws ZipException {
        extractFile(fileName, destPath, unzipParameters, null);
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
     * @param destPath
     * @param unzipParameters
     * @param newFileName
     * @throws ZipException
     */
    public void extractFile(String fileName, String destPath,
            UnzipParameters unzipParameters, String newFileName) throws ZipException {

        if (!Zip4jUtil.isStringNotNullAndNotEmpty(fileName)) {
            throw new ZipException("path to extract is null or empty, cannot extract path");
        }

        if (!Zip4jUtil.isStringNotNullAndNotEmpty(destPath)) {
            throw new ZipException("destination string path is empty or null, cannot extract path");
        }

        zipModel = readZipModel();

        CentralDirectory.FileHeader fileHeader = Zip4jUtil.getFileHeader(zipModel, fileName);

        if (fileHeader == null) {
            throw new ZipException("path header not found for given path name, cannot extract path");
        }

        if (progressMonitor.getState() == ProgressMonitor.STATE_BUSY) {
            throw new ZipException("invalid operation - Zip4j is in busy state");
        }

        fileHeader.extractFile(zipModel, destPath, unzipParameters, newFileName, progressMonitor, runInThread);

    }

    /**
     * Sets the password for the zip path.<br>
     * <b>Note</b>: For security reasons, usage of this method is discouraged. Use
     * setPassword(char[]) instead. As strings are immutable, they cannot be wiped
     * out from memory explicitly after usage. Therefore, usage of Strings to store
     * passwords is discouraged. More info here:
     * http://docs.oracle.com/javase/1.5.0/docs/guide/security/jce/JCERefGuide.html#PBEEx
     *
     * @param password
     * @throws ZipException
     */
    public void setPassword(String password) throws ZipException {
        if (!Zip4jUtil.isStringNotNullAndNotEmpty(password)) {
            throw new NullPointerException();
        }
        setPassword(password.toCharArray());
    }

    /**
     * Sets the password for the zip path
     *
     * @param password
     * @throws ZipException
     */
    public void setPassword(char[] password) throws ZipException {
        if (zipModel == null) {
            zipModel = readZipModel();
            if (zipModel == null) {
                throw new ZipException("Zip Model is null");
            }
        }

        if (zipModel.getCentralDirectory() == null || zipModel.getCentralDirectory().getFileHeaders() == null) {
            throw new ZipException("invalid zip path");
        }

        for (int i = 0; i < zipModel.getCentralDirectory().getFileHeaders().size(); i++) {
            if (zipModel.getCentralDirectory().getFileHeaders().get(i) != null) {
                if (zipModel.getCentralDirectory().getFileHeaders().get(i).isEncrypted()) {
                    zipModel.getCentralDirectory().getFileHeaders().get(i).setPassword(password);
                }
            }
        }
    }

    /**
     * Returns the list of path headers in the zip path. Throws an exception if the
     * zip path does not exist
     *
     * @return list of path headers
     * @throws ZipException
     */
    public List<CentralDirectory.FileHeader> getFileHeaders() throws ZipException {
        zipModel = readZipModel();
        return zipModel.getCentralDirectory().getFileHeaders();
    }

    /**
     * Returns FileHeader if a path header with the given fileHeader
     * string exists in the zip model: If not returns null
     *
     * @param fileName
     * @return FileHeader
     * @throws ZipException
     */
    public CentralDirectory.FileHeader getFileHeader(String fileName) throws ZipException {
        if (!Zip4jUtil.isStringNotNullAndNotEmpty(fileName)) {
            throw new ZipException("input path name is emtpy or null, cannot get FileHeader");
        }

        zipModel = readZipModel();

        return Zip4jUtil.getFileHeader(zipModel, fileName);
    }

    /**
     * Checks to see if the zip path is encrypted
     *
     * @return true if encrypted, false if not
     * @throws ZipException
     */
    public boolean isEncrypted() throws ZipException {
        zipModel = readZipModel();

        if (zipModel.getCentralDirectory() == null || zipModel.getCentralDirectory().getFileHeaders() == null) {
            throw new ZipException("invalid zip path");
        }

        List<CentralDirectory.FileHeader> fileHeaderList = zipModel.getCentralDirectory().getFileHeaders();
        for (int i = 0; i < fileHeaderList.size(); i++) {
            CentralDirectory.FileHeader fileHeader = fileHeaderList.get(i);
            if (fileHeader != null) {
                if (fileHeader.isEncrypted()) {
                    isEncrypted = true;
                    break;
                }
            }
        }

        return isEncrypted;
    }

    /**
     * Checks if the zip path is a split archive
     *
     * @return true if split archive, false if not
     * @throws ZipException
     */
    public boolean isSplitArchive() throws ZipException {
        zipModel = readZipModel();
        return zipModel.isSplitArchive();

    }

    /**
     * Removes the path provided in the input paramters from the zip path.
     * This method first finds the path header and then removes the path.
     * If path does not exist, then this method throws an exception.
     * If zip path is a split zip path, then this method throws an exception as
     * zip specification does not allow for updating split zip archives.
     *
     * @param fileName
     * @throws ZipException
     */
    public void removeFile(String fileName) throws ZipException {

        if (!Zip4jUtil.isStringNotNullAndNotEmpty(fileName)) {
            throw new ZipException("path name is empty or null, cannot remove path");
        }

        if (zipModel == null && Files.exists(path))
            zipModel = readZipModel();

        if (zipModel.isSplitArchive()) {
            throw new ZipException("Zip path format does not allow updating split/spanned files");
        }

        CentralDirectory.FileHeader fileHeader = Zip4jUtil.getFileHeader(zipModel, fileName);
        if (fileHeader == null) {
            throw new ZipException("could not find path header for path: " + fileName);
        }

        removeFile(fileHeader);
    }

    /**
     * Removes the path provided in the input path header from the zip path.
     * If zip path is a split zip path, then this method throws an exception as
     * zip specification does not allow for updating split zip archives.
     *
     * @param fileHeader
     * @throws ZipException
     */
    public void removeFile(CentralDirectory.FileHeader fileHeader) throws ZipException {
        if (fileHeader == null) {
            throw new ZipException("path header is null, cannot remove path");
        }

        if (zipModel == null && Files.exists(path))
            zipModel = readZipModel();

        if (zipModel.isSplitArchive()) {
            throw new ZipException("Zip path format does not allow updating split/spanned files");
        }

        ArchiveMaintainer archiveMaintainer = new ArchiveMaintainer();
        archiveMaintainer.initProgressMonitorForRemoveOp(zipModel, fileHeader, progressMonitor);
        archiveMaintainer.removeZipFile(zipModel, fileHeader, progressMonitor, runInThread);
    }

    /**
     * Merges split zip files into a single zip path without the need to extract the
     * files in the archive
     *
     * @param outputZipFile
     * @throws ZipException
     */
    public void mergeSplitFiles(File outputZipFile) throws ZipException {
        if (outputZipFile == null) {
            throw new ZipException("outputZipFile is null, cannot merge split files");
        }

        if (outputZipFile.exists()) {
            throw new ZipException("output Zip File already exists");
        }

        checkZipModel();

        if (this.zipModel == null) {
            throw new ZipException("zip model is null, corrupt zip path?");
        }

        ArchiveMaintainer archiveMaintainer = new ArchiveMaintainer();
        archiveMaintainer.initProgressMonitorForMergeOp(zipModel, progressMonitor);
        archiveMaintainer.mergeSplitZipFiles(zipModel, outputZipFile, progressMonitor, runInThread);
    }

    /**
     * Sets comment for the Zip path
     *
     * @param comment
     * @throws ZipException
     */
    public void setComment(String comment) throws ZipException {
        if (comment == null)
            throw new ZipException("input comment is null, cannot update zip path");

        if (!Files.exists(path))
            throw new ZipException("zip path does not exist, cannot set comment for zip path");

        zipModel = readZipModel();

        if (this.zipModel == null) {
            throw new ZipException("zipModel is null, cannot update zip path");
        }

        if (zipModel.getEndCentralDirectory() == null) {
            throw new ZipException("end of central directory is null, cannot set comment");
        }

        ArchiveMaintainer archiveMaintainer = new ArchiveMaintainer();
        archiveMaintainer.setComment(zipModel, comment);
    }

    /**
     * Returns the comment set for the Zip path
     *
     * @return String
     * @throws ZipException
     */
    public String getComment() throws ZipException {
        return getComment(Charset.defaultCharset());
    }

    /**
     * Returns the comment set for the Zip path in the input encoding
     *
     * @param charset
     * @return String
     * @throws ZipException
     */
    public String getComment(@NonNull Charset charset) throws ZipException {
        if (Files.exists(path))
            checkZipModel();
        else
            throw new ZipException("zip path does not exist, cannot read comment");

        if (zipModel == null)
            throw new ZipException("zip model is null, cannot read comment");

        if (zipModel.getEndCentralDirectory() == null)
            throw new ZipException("end of central directory record is null, cannot read comment");

        if (zipModel.getEndCentralDirectory().getComment() == null)
            return null;

        return zipModel.getEndCentralDirectory().getComment();
    }

    /**
     * Loads the zip model if zip model is null and if zip path exists.
     *
     * @throws ZipException
     */
    private void checkZipModel() throws ZipException {
        if (zipModel != null)
            return;
        zipModel = Files.exists(path) ? readZipModel() : createZipModel();
    }

    /**
     * Creates a new instance of zip model
     *
     * @throws ZipException
     */
    private ZipModel createZipModel() {
        ZipModel zipModel = new ZipModel();
        zipModel.setZipFile(path.toString());
        zipModel.setCharset(charset);
        return zipModel;
    }

    /**
     * Returns an input stream for reading the contents of the Zip path corresponding
     * to the input FileHeader. Throws an exception if the FileHeader does not exist
     * in the ZipFile
     *
     * @param fileHeader
     * @return ZipInputStream
     * @throws ZipException
     */
    public ZipInputStream getInputStream(CentralDirectory.FileHeader fileHeader) throws ZipException {
        if (fileHeader == null) {
            throw new ZipException("FileHeader is null, cannot get InputStream");
        }

        checkZipModel();

        if (zipModel == null) {
            throw new ZipException("zip model is null, cannot get inputstream");
        }

        Unzip unzip = new Unzip(zipModel);
        return unzip.getInputStream(fileHeader);
    }

    /**
     * Checks to see if the input zip path is a valid zip path. This method
     * will try to read zip headers. If headers are read successfully, this
     * method returns true else false
     *
     * @return boolean
     * @since 1.2.3
     */
    public boolean isValidZipFile() {
        try {
            zipModel = readZipModel();
            return true;
        } catch(Exception e) {
            return false;
        }
    }

    /**
     * Returns the full path path+names of all split zip files
     * in an ArrayList. For example: If a split zip path(abc.zip) has a 10 split parts
     * this method returns an array list with path + "abc.z01", path + "abc.z02", etc.
     * Returns null if the zip path does not exist
     *
     * @return ArrayList of Strings
     * @throws ZipException
     */
    public List<File> getSplitZipFiles() throws ZipException {
        checkZipModel();
        return Zip4jUtil.getSplitZipFiles(zipModel);
    }

    public ProgressMonitor getProgressMonitor() {
        return progressMonitor;
    }

    public boolean isRunInThread() {
        return runInThread;
    }

    public void setRunInThread(boolean runInThread) {
        this.runInThread = runInThread;
    }
}
