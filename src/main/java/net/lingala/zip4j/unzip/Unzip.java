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

import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.ZipInputStream;
import net.lingala.zip4j.model.CentralDirectory;
import net.lingala.zip4j.model.UnzipParameters;
import net.lingala.zip4j.model.ZipModel;
import net.lingala.zip4j.util.InternalZipConstants;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.List;

public class Unzip {

    private ZipModel zipModel;

    public Unzip(ZipModel zipModel) throws ZipException {

        if (zipModel == null) {
            throw new ZipException("ZipModel is null");
        }

        this.zipModel = zipModel;
    }

    public void extractAll(final UnzipParameters unzipParameters, final String outPath) throws ZipException {

        CentralDirectory centralDirectory = zipModel.getCentralDirectory();

        if (centralDirectory == null ||
                centralDirectory.getFileHeaders() == null) {
            throw new ZipException("invalid central directory in zipModel");
        }

        final List<CentralDirectory.FileHeader> fileHeaders = centralDirectory.getFileHeaders();

        initExtractAll(fileHeaders, unzipParameters, outPath);
    }

    private void initExtractAll(List<CentralDirectory.FileHeader> fileHeaders, UnzipParameters unzipParameters,
            String outPath) throws ZipException {

        for (int i = 0; i < fileHeaders.size(); i++) {
            CentralDirectory.FileHeader fileHeader = fileHeaders.get(i);
            initExtractFile(fileHeader, outPath, unzipParameters, null);
        }
    }

    public void extractFile(final CentralDirectory.FileHeader fileHeader, final String outPath,
            final UnzipParameters unzipParameters, final String newFileName) throws ZipException {
        if (fileHeader == null) {
            throw new ZipException("fileHeader is null");
        }

        initExtractFile(fileHeader, outPath, unzipParameters, newFileName);

    }

    private void initExtractFile(CentralDirectory.FileHeader fileHeader, String outPath,
            UnzipParameters unzipParameters, String newFileName) throws ZipException {

        if (fileHeader == null) {
            throw new ZipException("fileHeader is null");
        }

        try {
            if (!outPath.endsWith(InternalZipConstants.FILE_SEPARATOR)) {
                outPath += InternalZipConstants.FILE_SEPARATOR;
            }

            // If file header is a directory, then check if the directory exists
            // If not then create a directory and return
            if (fileHeader.isDirectory()) {
                try {
                    String fileName = fileHeader.getFileName();
                    if (StringUtils.isBlank(fileName)) {
                        return;
                    }
                    String completePath = outPath + fileName;
                    File file = new File(completePath);
                    if (!file.exists()) {
                        file.mkdirs();
                    }
                } catch(Exception e) {
                    throw new ZipException(e);
                }
            } else {
                //Create Directories
                checkOutputDirectoryStructure(fileHeader, outPath, newFileName);

                UnzipEngine unzipEngine = new UnzipEngine(zipModel, fileHeader);
                try {
                    unzipEngine.unzipFile(outPath, newFileName, unzipParameters);
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

    private void checkOutputDirectoryStructure(CentralDirectory.FileHeader fileHeader, String outPath, String newFileName) throws ZipException {
        if (fileHeader == null || StringUtils.isBlank(outPath)) {
            throw new ZipException("Cannot check output directory structure...one of the parameters was null");
        }

        String fileName = fileHeader.getFileName();

        if (StringUtils.isNotBlank(newFileName)) {
            fileName = newFileName;
        }

        if (StringUtils.isBlank(fileName)) {
            // Do nothing
            return;
        }

        String compOutPath = outPath + fileName;
        try {
            File file = new File(compOutPath);
            String parentDir = file.getParent();
            File parentDirFile = new File(parentDir);
            if (!parentDirFile.exists()) {
                parentDirFile.mkdirs();
            }
        } catch(Exception e) {
            throw new ZipException(e);
        }
    }

    private long calculateTotalWork(List<CentralDirectory.FileHeader> fileHeaders) throws ZipException {

        if (fileHeaders == null) {
            throw new ZipException("fileHeaders is null, cannot calculate total work");
        }

        long totalWork = 0;

        for (int i = 0; i < fileHeaders.size(); i++) {
            CentralDirectory.FileHeader fileHeader = fileHeaders.get(i);
            if (fileHeader.getZip64ExtendedInfo() != null &&
                    fileHeader.getZip64ExtendedInfo().getUnCompressedSize() > 0) {
                totalWork += fileHeader.getZip64ExtendedInfo().getCompressedSize();
            } else {
                totalWork += fileHeader.getCompressedSize();
            }

        }

        return totalWork;
    }

}
