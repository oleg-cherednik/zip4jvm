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

package net.lingala.zip4j.zip;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.SplitOutputStream;
import net.lingala.zip4j.io.ZipOutputStream;
import net.lingala.zip4j.model.CentralDirectory;
import net.lingala.zip4j.model.ZipModel;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.ArchiveMaintainer;
import net.lingala.zip4j.util.ChecksumCalculator;
import net.lingala.zip4j.util.InternalZipConstants;
import net.lingala.zip4j.util.Zip4jConstants;
import net.lingala.zip4j.util.Zip4jUtil;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ZipEngine {

    @NonNull
    private final ZipModel zipModel;

    public void addFiles(@NonNull Collection<Path> files, @NonNull ZipParameters parameters, boolean runInThread) throws ZipException, IOException {
        if (files.isEmpty())
            return;

        if (runInThread) {
            Thread thread = new Thread(InternalZipConstants.THREAD_NAME) {
                @Override
                public void run() {
                    try {
                        initAddFiles(files, parameters);
                    } catch(ZipException e) {
                    }
                }
            };
            thread.start();

        } else {
            initAddFiles(files, parameters);
        }
    }

    private void initAddFiles(@NonNull Collection<Path> files, @NonNull ZipParameters parameters) throws ZipException {
        zipModel.createEndCentralDirectoryIfNotExist();


        ZipOutputStream outputStream = null;

        try {
            checkParameters(parameters);
            removeFilesIfExists(files, parameters);

            boolean isZipFileAlreadExists = Files.exists(zipModel.getZipFile());
            SplitOutputStream splitOutputStream = new SplitOutputStream(zipModel.getZipFile().toFile(), zipModel.getSplitLength());
            outputStream = new ZipOutputStream(splitOutputStream, zipModel);

            if (isZipFileAlreadExists) {
                if (zipModel.getEndCentralDirectory() == null) {
                    throw new ZipException("invalid end of central directory record");
                }
                splitOutputStream.seek(zipModel.getEndCentralDirectory().getOffOfStartOfCentralDir());
            }

            byte[] buf = new byte[InternalZipConstants.BUFF_SIZE];
            int readLen = -1;

            for (Path file : files) {
                String fileName = Zip4jUtil.getRelativeFileName(file, parameters);

                if ("/".equals(fileName) || "\\".equals(fileName))
                    continue;

                ZipParameters fileParameters = (ZipParameters)parameters.clone();

                if (Files.isRegularFile(file)) {
                    if (fileParameters.isEncryptFiles() && fileParameters.getEncryptionMethod() == Zip4jConstants.ENC_METHOD_STANDARD)
                        fileParameters.setSourceFileCRC(new ChecksumCalculator(file).calculate());

                    if (Files.size(file) == 0)
                        fileParameters.setCompressionMethod(Zip4jConstants.COMP_STORE);
                }

                outputStream.putNextEntry(file.toFile(), fileParameters);

                if (Files.isDirectory(file)) {
                    outputStream.closeEntry();
                    continue;
                }

                try (InputStream inputStream = new FileInputStream(file.toFile())) {
                    IOUtils.copyLarge(inputStream, outputStream);
                }

                outputStream.closeEntry();
            }

            outputStream.finish();
        } catch(ZipException e) {
            throw e;
        } catch(Exception e) {
            throw new ZipException(e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch(IOException e) {
                }
            }
        }
    }

    public void addStreamToZip(InputStream inputStream, ZipParameters parameters) throws ZipException {
        if (inputStream == null || parameters == null) {
            throw new ZipException("one of the input parameters is null, cannot add stream to zip");
        }

        ZipOutputStream outputStream = null;

        try {
            checkParameters(parameters);

            boolean isZipFileAlreadExists = Files.exists(zipModel.getZipFile());

            SplitOutputStream splitOutputStream = new SplitOutputStream(zipModel.getZipFile().toFile(), zipModel.getSplitLength());
            outputStream = new ZipOutputStream(splitOutputStream, this.zipModel);

            if (isZipFileAlreadExists) {
                if (zipModel.getEndCentralDirectory() == null) {
                    throw new ZipException("invalid end of central directory record");
                }
                splitOutputStream.seek(zipModel.getEndCentralDirectory().getOffOfStartOfCentralDir());
            }

            byte[] readBuff = new byte[InternalZipConstants.BUFF_SIZE];
            int readLen = -1;

            outputStream.putNextEntry(null, parameters);

            if (!parameters.getFileNameInZip().endsWith("/") &&
                    !parameters.getFileNameInZip().endsWith("\\")) {
                while ((readLen = inputStream.read(readBuff)) != -1) {
                    outputStream.write(readBuff, 0, readLen);
                }
            }

            outputStream.closeEntry();
            outputStream.finish();

        } catch(ZipException e) {
            throw e;
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
        }
    }

    public void addFolderToZip(File file, ZipParameters parameters, boolean runInThread) throws ZipException, IOException {
        if (file == null || parameters == null) {
            throw new ZipException("one of the input parameters is null, cannot add folder to zip");
        }

        if (!Zip4jUtil.checkFileExists(file.getAbsolutePath())) {
            throw new ZipException("input folder does not exist");
        }

        if (!file.isDirectory()) {
            throw new ZipException("input file is not a folder, user addFileToZip method to add files");
        }

        if (!Zip4jUtil.checkFileReadAccess(file.getAbsolutePath())) {
            throw new ZipException("cannot read folder: " + file.getAbsolutePath());
        }

        String rootFolderPath = null;
        if (parameters.isIncludeRootFolder()) {
            if (file.getAbsolutePath() != null) {
                rootFolderPath = file.getAbsoluteFile().getParentFile() != null ? file.getAbsoluteFile().getParentFile().getAbsolutePath() : "";
            } else {
                rootFolderPath = file.getParentFile() != null ? file.getParentFile().getAbsolutePath() : "";
            }
        } else {
            rootFolderPath = file.getAbsolutePath();
        }

        parameters.setDefaultFolderPath(rootFolderPath);

        List<Path> files = Zip4jUtil.getFilesInDirectoryRec(file, parameters.isReadHiddenFiles()).stream()
                                    .map(File::toPath)
                                    .collect(Collectors.toList());

        if (parameters.isIncludeRootFolder()) {
            if (files == null) {
                files = new ArrayList();
            }
            files.add(file.toPath());
        }

        addFiles(files, parameters, runInThread);
    }


    private void checkParameters(ZipParameters parameters) throws ZipException {
        if ((parameters.getCompressionMethod() != Zip4jConstants.COMP_STORE) &&
                parameters.getCompressionMethod() != Zip4jConstants.COMP_DEFLATE) {
            throw new ZipException("unsupported compression type");
        }

        if (parameters.getCompressionMethod() == Zip4jConstants.COMP_DEFLATE) {
            if (parameters.getCompressionLevel() < 0 && parameters.getCompressionLevel() > 9) {
                throw new ZipException("invalid compression level. compression level dor deflate should be in the range of 0-9");
            }
        }

        if (parameters.isEncryptFiles()) {
            if (parameters.getEncryptionMethod() != Zip4jConstants.ENC_METHOD_STANDARD &&
                    parameters.getEncryptionMethod() != Zip4jConstants.ENC_METHOD_AES) {
                throw new ZipException("unsupported encryption method");
            }

            if (parameters.getPassword() == null || parameters.getPassword().length <= 0) {
                throw new ZipException("input password is empty or null");
            }
        } else {
            parameters.setAesKeyStrength(-1);
            parameters.setEncryptionMethod(-1);
        }

    }

    public void removeFile(CentralDirectory.FileHeader fileHeader, boolean runInThread) throws ZipException {
        if (fileHeader == null)
            return;

        ArchiveMaintainer archiveMaintainer = new ArchiveMaintainer();
        archiveMaintainer.removeZipFile(zipModel, fileHeader, runInThread);
    }


    /**
     * Before adding a file to a zip file, we check if a file already exists in the zip file
     * with the same fileName (including path, if exists). If yes, then we remove this file
     * before adding the file<br><br>
     *
     * <b>Note:</b> Relative path has to be passed as the fileName
     *
     * @throws ZipException
     */
    private void removeFilesIfExists(@NonNull Collection<Path> files, ZipParameters parameters) throws ZipException {
        if (zipModel == null || zipModel.isEmpty())
            return;

        for (Path file : files)
            removeFile(zipModel.getFileHeader(Zip4jUtil.getRelativeFileName(file, parameters)), false);
    }

    private RandomAccessFile prepareFileOutputStream() throws ZipException {
        try {
            Files.createDirectories(zipModel.getZipFile().getParent());
            return new RandomAccessFile(zipModel.getZipFile().toFile(), InternalZipConstants.WRITE_MODE);
        } catch(Exception e) {
            throw new ZipException(e);
        }
    }

    private long calculateTotalWork(@NonNull Collection<Path> files, ZipParameters parameters) throws ZipException, IOException {
        long totalWork = 0;

        for (Path file : files) {
            if (!Files.exists(file))
                continue;

            if (parameters.isEncryptFiles() && parameters.getEncryptionMethod() == Zip4jConstants.ENC_METHOD_STANDARD)
                totalWork += Files.size(file) * 2;
            else
                totalWork += Files.size(file);

            if (zipModel.getCentralDirectory() != null && !zipModel.getCentralDirectory().getFileHeaders().isEmpty()) {
                String relativeFileName = Zip4jUtil.getRelativeFileName(
                        file.toAbsolutePath().toString(), parameters.getRootFolderInZip(), parameters.getDefaultFolderPath());
                CentralDirectory.FileHeader fileHeader = zipModel.getFileHeader(relativeFileName);
                if (fileHeader != null) {
                    totalWork += Files.size(zipModel.getZipFile()) - fileHeader.getCompressedSize();
                }
            }
        }

        return totalWork;
    }
}
