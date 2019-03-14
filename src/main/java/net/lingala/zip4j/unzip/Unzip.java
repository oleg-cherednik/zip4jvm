/*
 * Copyright 2010 Srikanth Reddy Lingala
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package net.lingala.zip4j.unzip;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.ZipInputStream;
import net.lingala.zip4j.model.CentralDirectory;
import net.lingala.zip4j.model.UnzipParameters;
import net.lingala.zip4j.model.ZipModel;
import org.apache.commons.lang.StringUtils;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 14.03.2019
 */
@RequiredArgsConstructor
public final class Unzip {

    @NonNull
    private final ZipModel zipModel;

    public void extract(@NonNull Path destDir, @NonNull UnzipParameters unzipParameters) throws ZipException {
        CentralDirectory centralDirectory = zipModel.getCentralDirectory();

        if (centralDirectory == null)
            throw new ZipException("invalid central directory in zipModel");

        for (CentralDirectory.FileHeader fileHeader : centralDirectory.getFileHeaders())
            initExtractFile(fileHeader, destDir, unzipParameters, null);
    }

    public void extractFile(final CentralDirectory.FileHeader fileHeader, final Path destDir,
            final UnzipParameters unzipParameters, final String newFileName) throws ZipException {
        if (fileHeader == null) {
            throw new ZipException("fileHeader is null");
        }

        initExtractFile(fileHeader, destDir, unzipParameters, newFileName);

    }

    private void initExtractFile(@NonNull CentralDirectory.FileHeader fileHeader, @NonNull Path destDir, UnzipParameters unzipParameters,
            String newFileName) throws ZipException {

        try {
            // If file header is a directory, then check if the directory exists
            // If not then create a directory and return
            if (fileHeader.isDirectory()) {
                try {
                    String fileName = fileHeader.getFileName();

                    if (StringUtils.isBlank(fileName))
                        return;

                    Path completePath = destDir.resolve(fileName);

                    if (!Files.exists(completePath))
                        Files.createDirectories(completePath);
                } catch(Exception e) {
                    throw new ZipException(e);
                }
            } else {
                //Create Directories
                checkOutputDirectoryStructure(fileHeader, destDir, newFileName);

                UnzipEngine unzipEngine = new UnzipEngine(zipModel, fileHeader);
                try {
                    unzipEngine.unzipFile(destDir, newFileName, unzipParameters);
                } catch(Exception e) {
                    throw new ZipException(e);
                }
            }
        } catch(ZipException e) {
            throw e;
        } catch(Exception e) {
            throw new ZipException(e);
        }
    }

    public ZipInputStream getInputStream(CentralDirectory.FileHeader fileHeader) throws ZipException {
        UnzipEngine unzipEngine = new UnzipEngine(zipModel, fileHeader);
        return unzipEngine.getInputStream();
    }

    private void checkOutputDirectoryStructure(@NonNull CentralDirectory.FileHeader fileHeader, @NonNull Path destDir, String newFileName)
            throws ZipException {
        String fileName = fileHeader.getFileName();

        if (StringUtils.isNotBlank(newFileName))
            fileName = newFileName;

        if (StringUtils.isBlank(fileName)) {
            // Do nothing
            return;
        }

        Path compOutPath = destDir.resolve(fileName);
        try {
            Path parentDir = compOutPath.getParent();

            if (!Files.exists(parentDir))
                Files.createDirectories(parentDir);
        } catch(Exception e) {
            throw new ZipException(e);
        }
    }

}
