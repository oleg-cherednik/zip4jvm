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
import net.lingala.zip4j.engine.UnzipEngine;
import net.lingala.zip4j.engine.ZipEngine;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.ZipInputStream;
import net.lingala.zip4j.model.CentralDirectory;
import net.lingala.zip4j.model.ZipModel;
import net.lingala.zip4j.util.ArchiveMaintainer;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

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

    private void checkSplitArchiveModification() throws ZipException {
        if (Files.exists(zipFile) && zipModel.isSplitArchive())
            throw new ZipException("Zip file already exists. Zip file format does not allow updating split/spanned files");
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
        zipModel = createZipModel(zipFile, charset);
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

        zipModel = createZipModel(zipFile, charset);

        if (this.zipModel == null) {
            throw new ZipException("zip model is null, corrupt zip path?");
        }

        ArchiveMaintainer archiveMaintainer = new ArchiveMaintainer();
        archiveMaintainer.mergeSplitZipFiles(zipModel, outputZipFile);
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

        zipModel = createZipModel(zipFile, charset);

        if (zipModel == null)
            throw new ZipException("zip model is null, cannot get inputstream");

        return new UnzipEngine(zipModel).getInputStream();
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

}
