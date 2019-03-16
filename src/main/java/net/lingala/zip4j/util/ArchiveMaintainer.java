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

package net.lingala.zip4j.util;

import lombok.NonNull;
import net.lingala.zip4j.core.HeaderWriter;
import net.lingala.zip4j.core.readers.LocalFileHeaderReader;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.NoSplitOutputStream;
import net.lingala.zip4j.io.SplitOutputStream;
import net.lingala.zip4j.model.CentralDirectory;
import net.lingala.zip4j.model.EndCentralDirectory;
import net.lingala.zip4j.model.LocalFileHeader;
import net.lingala.zip4j.model.ZipModel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Paths;
import java.util.List;

public class ArchiveMaintainer {

    public void removeZipFile(ZipModel zipModel, CentralDirectory.FileHeader fileHeader) throws ZipException {
        if (fileHeader == null)
            return;

        initRemoveZipFile(zipModel, fileHeader);
    }

    private void initRemoveZipFile(@NonNull ZipModel zipModel, @NonNull CentralDirectory.FileHeader fileHeader) throws ZipException {
        SplitOutputStream out = null;
        File zipFile = null;
        RandomAccessFile inputStream = null;
        boolean successFlag = false;
        String tmpZipFileName = null;

        try {
            int indexOfFileHeader = Zip4jUtil.getIndexOfFileHeader(zipModel, fileHeader);

            if (indexOfFileHeader < 0) {
                throw new ZipException("file header not found in zip model, cannot remove file");
            }

            if (zipModel.isSplitArchive()) {
                throw new ZipException("This is a split archive. Zip file format does not allow updating split/spanned files");
            }

            long currTime = System.currentTimeMillis();
            tmpZipFileName = zipModel.getZipFile().toString() + currTime % 1000;
            File tmpFile = new File(tmpZipFileName);

            while (tmpFile.exists()) {
                currTime = System.currentTimeMillis();
                tmpZipFileName = zipModel.getZipFile().toString() + currTime % 1000;
                tmpFile = new File(tmpZipFileName);
            }

            try {
                out = new NoSplitOutputStream(Paths.get(tmpZipFileName));
            } catch(FileNotFoundException e1) {
                throw new ZipException(e1);
            }

            zipFile = zipModel.getZipFile().toFile();

            inputStream = createFileHandler(zipModel, InternalZipConstants.READ_MODE);

            LocalFileHeader localFileHeader = new LocalFileHeaderReader(new LittleEndianRandomAccessFile(inputStream), fileHeader).read();
            if (localFileHeader == null) {
                throw new ZipException("invalid local file header, cannot remove file from archive");
            }

            long offsetLocalFileHeader = fileHeader.getOffLocalHeaderRelative();

            if (fileHeader.getZip64ExtendedInfo() != null &&
                    fileHeader.getZip64ExtendedInfo().getOffsLocalHeaderRelative() != -1) {
                offsetLocalFileHeader = fileHeader.getZip64ExtendedInfo().getOffsLocalHeaderRelative();
            }

            long offsetEndOfCompressedFile = -1;

            long offsetStartCentralDir = zipModel.getEndCentralDirectory().getOffOfStartOfCentralDir();
            if (zipModel.isZip64Format()) {
                if (zipModel.getZip64EndCentralDirectory() != null) {
                    offsetStartCentralDir = zipModel.getZip64EndCentralDirectory().getOffsetStartCenDirWRTStartDiskNo();
                }
            }

            List<CentralDirectory.FileHeader> fileHeaders = zipModel.getFileHeaders();

            if (indexOfFileHeader == fileHeaders.size() - 1) {
                offsetEndOfCompressedFile = offsetStartCentralDir - 1;
            } else {
                CentralDirectory.FileHeader nextFileHeader = fileHeaders.get(indexOfFileHeader + 1);
                if (nextFileHeader != null) {
                    offsetEndOfCompressedFile = nextFileHeader.getOffLocalHeaderRelative() - 1;
                    if (nextFileHeader.getZip64ExtendedInfo() != null &&
                            nextFileHeader.getZip64ExtendedInfo().getOffsLocalHeaderRelative() != -1) {
                        offsetEndOfCompressedFile = nextFileHeader.getZip64ExtendedInfo().getOffsLocalHeaderRelative() - 1;
                    }
                }
            }

            if (offsetLocalFileHeader < 0 || offsetEndOfCompressedFile < 0) {
                throw new ZipException("invalid offset for start and end of local file, cannot remove file");
            }

            if (indexOfFileHeader == 0) {
                if (zipModel.getFileHeaders().size() > 1) {
                    // if this is the only file and it is deleted then no need to do this
                    copyFile(inputStream, out, offsetEndOfCompressedFile + 1, offsetStartCentralDir);
                }
            } else if (indexOfFileHeader == fileHeaders.size() - 1) {
                copyFile(inputStream, out, 0, offsetLocalFileHeader);
            } else {
                copyFile(inputStream, out, 0, offsetLocalFileHeader);
                copyFile(inputStream, out, offsetEndOfCompressedFile + 1, offsetStartCentralDir);
            }

            EndCentralDirectory endCentralDirectory = zipModel.getEndCentralDirectory();
            endCentralDirectory.setOffOfStartOfCentralDir(out.getFilePointer());
            endCentralDirectory.setTotNoOfEntriesInCentralDir(endCentralDirectory.getTotNoOfEntriesInCentralDir() - 1);
            endCentralDirectory.setTotalNumberOfEntriesInCentralDirOnThisDisk(
                    endCentralDirectory.getTotalNumberOfEntriesInCentralDirOnThisDisk() - 1);

            zipModel.getFileHeaders().remove(indexOfFileHeader);

            for (int i = indexOfFileHeader; i < zipModel.getFileHeaders().size(); i++) {
                long offsetLocalHdr = zipModel.getFileHeaders().get(i).getOffLocalHeaderRelative();
                if (zipModel.getFileHeaders().get(i).getZip64ExtendedInfo() != null &&
                        zipModel.getFileHeaders().get(i).getZip64ExtendedInfo().getOffsLocalHeaderRelative() != -1) {
                    offsetLocalHdr = zipModel.getFileHeaders().get(i).getZip64ExtendedInfo().getOffsLocalHeaderRelative();
                }

                zipModel.getFileHeaders().get(i).setOffLocalHeaderRelative(
                        offsetLocalHdr - (offsetEndOfCompressedFile - offsetLocalFileHeader) - 1);
            }

            HeaderWriter headerWriter = new HeaderWriter();
            headerWriter.finalizeZipFile(zipModel, out);

            successFlag = true;
        } catch(ZipException e) {
            throw e;
        } catch(Exception e) {
            throw new ZipException(e);
        } finally {
            try {
                if (inputStream != null)
                    inputStream.close();
                if (out != null)
                    out.close();
            } catch(IOException e) {
                throw new ZipException("cannot close input stream or output stream when trying to delete a file from zip file");
            }

            if (successFlag) {
                restoreFileName(zipFile, tmpZipFileName);
            } else {
                File newZipFile = new File(tmpZipFileName);
                newZipFile.delete();
            }
        }
    }

    private void restoreFileName(File zipFile, String tmpZipFileName) throws ZipException {
        if (zipFile.delete()) {
            File newZipFile = new File(tmpZipFileName);
            if (!newZipFile.renameTo(zipFile)) {
                throw new ZipException("cannot rename modified zip file");
            }
        } else {
            throw new ZipException("cannot delete old zip file");
        }
    }

    public static void copyFile(RandomAccessFile inputStream, OutputStream outputStream, long start, long end) throws ZipException {

        if (inputStream == null || outputStream == null) {
            throw new ZipException("input or output stream is null, cannot copy file");
        }

        if (start < 0) {
            throw new ZipException("starting offset is negative, cannot copy file");
        }

        if (end < 0) {
            throw new ZipException("end offset is negative, cannot copy file");
        }

        if (start > end) {
            throw new ZipException("start offset is greater than end offset, cannot copy file");
        }

        if (start == end) {
            return;
        }

        try {
            inputStream.seek(start);

            int readLen = -2;
            byte[] buff;
            long bytesRead = 0;
            long bytesToRead = end - start;

            if ((end - start) < InternalZipConstants.BUFF_SIZE) {
                buff = new byte[(int)(end - start)];
            } else {
                buff = new byte[InternalZipConstants.BUFF_SIZE];
            }

            while ((readLen = inputStream.read(buff)) != -1) {
                outputStream.write(buff, 0, readLen);

                bytesRead += readLen;

                if (bytesRead == bytesToRead) {
                    break;
                } else if (bytesRead + buff.length > bytesToRead) {
                    buff = new byte[(int)(bytesToRead - bytesRead)];
                }
            }

        } catch(IOException e) {
            throw new ZipException(e);
        } catch(Exception e) {
            throw new ZipException(e);
        }
    }

    private static RandomAccessFile createFileHandler(ZipModel zipModel, String mode) throws ZipException {
        if (zipModel == null || zipModel.getZipFile() == null)
            throw new ZipException("input parameter is null in getFilePointer, cannot create file handler to remove file");

        try {
            return new RandomAccessFile(zipModel.getZipFile().toFile(), mode);
        } catch(FileNotFoundException e) {
            throw new ZipException(e);
        }
    }

}
