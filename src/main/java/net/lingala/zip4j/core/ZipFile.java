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
import net.lingala.zip4j.model.Encryption;
import net.lingala.zip4j.model.InputStreamMeta;
import net.lingala.zip4j.model.ZipModel;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.engine.UnzipEngine;
import net.lingala.zip4j.util.ArchiveMaintainer;
import net.lingala.zip4j.util.Zip4jUtil;
import net.lingala.zip4j.engine.ZipEngine;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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

    @NonNull
    private final Path zipFile;
    @NonNull
    private Charset charset = Charset.defaultCharset();

    private ZipModel zipModel;
    private boolean isEncrypted;

    /**
     * Adds the list of input files to the zip path. If zip path does not exist, then
     * this method creates a new zip path. Parameters such as compression type, etc
     * can be set in the input parameters.
     *
     * @param files
     * @param parameters
     * @throws ZipException
     */
    public void addFiles(Collection<Path> files, ZipParameters parameters) throws ZipException, IOException {
        Objects.requireNonNull(files);
        Objects.requireNonNull(parameters);

        zipModel = readOrCreateModel();
        zipModel.setSplitLength(parameters.getSplitLength());
        checkSplitArchiveModification();

        new ZipEngine(zipModel).addEntries(files, parameters);
    }

    private void checkSplitArchiveModification() throws ZipException {
        if (Files.exists(zipFile) && zipModel.isSplitArchive())
            throw new ZipException("Zip file already exists. Zip file format does not allow updating split/spanned files");
    }

    /**
     * Creates a new entry in the zip path and adds the content of the inputstream to the
     * zip path. ZipParameters.isSourceExternalStream and ZipParameters.fileNameInZip have to be
     * set before in the input parameters. If the path name ends with / or \, this method treats the
     * content as a directory. Setting the flag ProgressMonitor.setRunInThread to true will have
     * no effect for this method and hence this method cannot be used to add content to zip in
     * thread mode
     *
     * @param parameters
     * @throws ZipException
     */
    public void addStream(@NonNull Collection<InputStreamMeta> files, @NonNull ZipParameters parameters) throws ZipException {
        readOrCreateModel();

        if (Files.exists(zipFile) && zipModel.isSplitArchive())
            throw new ZipException("Zip path already exists. Zip path format does not allow updating split/spanned files");

        new ZipEngine(zipModel).addStreamToZip(files, parameters);
    }

    public void addStream(@NonNull InputStreamMeta file, @NonNull ZipParameters parameters) throws ZipException {
        addStream(Collections.singletonList(file), parameters);
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
        if (StringUtils.isBlank(password)) {
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
            zipModel = createZipModel();
            if (zipModel == null) {
                throw new ZipException("Zip Model is null");
            }
        }

        zipModel.getCentralDirectory().getFileHeaders().stream()
                .filter(fileHeader -> fileHeader.getEncryption() != Encryption.AES)
                .forEach(fileHeader -> fileHeader.setPassword(password));
    }

    /**
     * Returns the list of path headers in the zip path. Throws an exception if the
     * zip path does not exist
     *
     * @return list of path headers
     * @throws ZipException
     */
    public List<CentralDirectory.FileHeader> getFileHeaders() throws ZipException {
        zipModel = createZipModel();
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
        if (StringUtils.isBlank(fileName)) {
            throw new ZipException("input path name is emtpy or null, cannot get FileHeader");
        }

        zipModel = createZipModel();

        return zipModel.getFileHeader(fileName);
    }

    /**
     * Checks to see if the zip path is encrypted
     *
     * @return true if encrypted, false if not
     * @throws ZipException
     */
    public boolean isEncrypted() throws ZipException {
        zipModel = createZipModel();

        isEncrypted = zipModel.getCentralDirectory().getFileHeaders().stream()
                              .anyMatch(fileHeader -> fileHeader.getEncryption() != Encryption.OFF);

        return isEncrypted;
    }

    /**
     * Checks if the zip path is a split archive
     *
     * @return true if split archive, false if not
     * @throws ZipException
     */
    public boolean isSplitArchive() throws ZipException {
        zipModel = createZipModel();
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

        if (StringUtils.isBlank(fileName)) {
            throw new ZipException("path name is empty or null, cannot remove path");
        }

        if (zipModel == null && Files.exists(zipFile))
            zipModel = createZipModel();

        if (zipModel.isSplitArchive()) {
            throw new ZipException("Zip path format does not allow updating split/spanned files");
        }

        CentralDirectory.FileHeader fileHeader = zipModel.getFileHeader(fileName);
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
    public void removeFile(@NonNull CentralDirectory.FileHeader fileHeader) throws ZipException {
        readOrCreateModel();
        checkSplitArchiveModification();

        new ZipEngine(zipModel).removeFile(fileHeader);
    }

    /**
     * Merges split zip files into a single zip path without the need to extractEntries the
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

        readOrCreateModel();

        if (this.zipModel == null) {
            throw new ZipException("zip model is null, corrupt zip path?");
        }

        ArchiveMaintainer archiveMaintainer = new ArchiveMaintainer();
        archiveMaintainer.mergeSplitZipFiles(zipModel, outputZipFile);
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

        if (!Files.exists(zipFile))
            throw new ZipException("zip path does not exist, cannot set comment for zip path");

        zipModel = createZipModel();

        if (zipModel == null) {
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
        if (Files.exists(zipFile))
            readOrCreateModel();
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
     * Returns an input stream for reading the contents of the Zip path corresponding
     * to the input FileHeader. Throws an exception if the FileHeader does not exist
     * in the ZipFile
     *
     * @param fileHeader
     * @return ZipInputStream
     * @throws ZipException
     */
    public ZipInputStream getInputStream(CentralDirectory.FileHeader fileHeader) throws ZipException, IOException {
        if (fileHeader == null) {
            throw new ZipException("FileHeader is null, cannot get InputStream");
        }

        readOrCreateModel();

        if (zipModel == null)
            throw new ZipException("zip model is null, cannot get inputstream");

        return new UnzipEngine(zipModel).getInputStream();
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
            zipModel = createZipModel();
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
        readOrCreateModel();
        return Zip4jUtil.getSplitZipFiles(zipModel);
    }

    /**
     * Reads the zip header information for this zip path. If the zip path
     * does not exist, then this method throws an exception.<br><br>
     * <b>Note:</b> This method does not read local path header information
     *
     * @throws ZipException
     */
    private ZipModel createZipModel() throws ZipException {
        return createZipModel(zipFile, charset);
    }

    @NonNull
    public static ZipModel createZipModel(@NonNull Path zipFile, @NonNull Charset charset) throws ZipException {
        try {
            if (Files.exists(zipFile))
                return new ZipModelReader(zipFile, charset).read();

            ZipModel zipModel = new ZipModel();
            zipModel.setZipFile(zipFile);
            zipModel.setCharset(charset);
            return zipModel;
        } catch(IOException e) {
            throw new ZipException(e);
        }
    }

    private ZipModel readOrCreateModel() throws ZipException {
        return createZipModel(zipFile, charset);
    }
}
