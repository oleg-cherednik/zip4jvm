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

package net.lingala.zip4j.engine;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.core.readers.LocalFileHeaderReader;
import net.lingala.zip4j.crypto.AESDecrypter;
import net.lingala.zip4j.crypto.IDecrypter;
import net.lingala.zip4j.crypto.StandardDecrypter;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.InflaterInputStream;
import net.lingala.zip4j.io.PartInputStream;
import net.lingala.zip4j.io.ZipInputStream;
import net.lingala.zip4j.model.AESExtraDataRecord;
import net.lingala.zip4j.model.AESStrength;
import net.lingala.zip4j.model.CentralDirectory;
import net.lingala.zip4j.model.CompressionMethod;
import net.lingala.zip4j.model.Encryption;
import net.lingala.zip4j.model.LocalFileHeader;
import net.lingala.zip4j.model.ZipModel;
import net.lingala.zip4j.util.InternalZipConstants;
import net.lingala.zip4j.util.LittleEndianRandomAccessFile;
import net.lingala.zip4j.util.Raw;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.zip.CRC32;

/**
 * @author Oleg Cherednik
 * @since 14.03.2019
 */
@RequiredArgsConstructor
public class UnzipEngine {

    @NonNull
    private final ZipModel zipModel;
    private CRC32 crc = new CRC32();

    private CentralDirectory.FileHeader fileHeader;

    private int currSplitFileCounter = 0;
    private LocalFileHeader localFileHeader;
    private IDecrypter decrypter;

    public void extractEntries(@NonNull Path destDir) throws ZipException, IOException {
        extractEntries(destDir, zipModel.getEntryNames());
    }

    public void extractEntries(@NonNull Path destDir, @NonNull Collection<String> entries) throws ZipException, IOException {
        for (CentralDirectory.FileHeader fileHeader : getFileHeaders(entries)) {
            // TODO temporary
            this.fileHeader = fileHeader;
            crc = new CRC32();
            extractEntry(destDir, fileHeader);
        }
    }

    private List<CentralDirectory.FileHeader> getFileHeaders(Collection<String> entries) throws ZipException {
        List<CentralDirectory.FileHeader> fileHeaders = entries.stream()
                                                               .map(entryName -> zipModel.getCentralDirectory().getFileHeaderByName(entryName))
                                                               .filter(Objects::nonNull)
                                                               .collect(Collectors.toList());

        if (fileHeaders.size() == entries.size())
            return fileHeaders;

        throw new ZipException("Not all entries were found");
    }

    public void extractEntry(@NonNull Path destDir) throws ZipException, IOException {
        extractEntry(destDir, fileHeader);
    }

    private void extractEntry(Path destDir, CentralDirectory.FileHeader fileHeader) throws ZipException, IOException {
        if (fileHeader.isDirectory())
            Files.createDirectories(destDir.resolve(fileHeader.getFileName()));
        else {
            try (InputStream in = getInputStream(); OutputStream out = getOutputStream(destDir)) {
                IOUtils.copyLarge(in, out);
            }
        }
    }

    public ZipInputStream getInputStream() throws ZipException, IOException {
        RandomAccessFile raf = createFileHandler("r");
        String errMsg = "local header and file header do not match";
        //checkSplitFile();

        if (!checkLocalHeader())
            throw new ZipException(errMsg);

        init(raf);

        long comprSize = localFileHeader.getCompressedSize();
        long offsetStartOfData = localFileHeader.getOffsetStartOfData();

        if (localFileHeader.getEncryption() == Encryption.AES) {
            if (decrypter instanceof AESDecrypter) {
                comprSize -= ((AESDecrypter)decrypter).getSaltLength() +
                        ((AESDecrypter)decrypter).getPasswordVerifierLength() + 10;
                offsetStartOfData += ((AESDecrypter)decrypter).getSaltLength() +
                        ((AESDecrypter)decrypter).getPasswordVerifierLength();
            } else {
                throw new ZipException("invalid decryptor when trying to calculate " +
                        "compressed size for AES encrypted file: " + fileHeader.getFileName());
            }
        } else if (localFileHeader.getEncryption() == Encryption.STANDARD) {
            comprSize -= InternalZipConstants.STD_DEC_HDR_SIZE;
            offsetStartOfData += InternalZipConstants.STD_DEC_HDR_SIZE;
        }

        CompressionMethod compressionMethod = fileHeader.getCompressionMethod();
        if (fileHeader.getEncryption() == Encryption.AES) {
            if (fileHeader.getAesExtraDataRecord() != null)
                compressionMethod = fileHeader.getAesExtraDataRecord().getCompressionMethod();
            else
                throw new ZipException("AESExtraDataRecord does not exist for AES encrypted file: " + fileHeader.getFileName());
        }
        raf.seek(offsetStartOfData);

        if (compressionMethod == CompressionMethod.STORE)
            return new ZipInputStream(new PartInputStream(raf, offsetStartOfData, comprSize, this), this);
        if (compressionMethod == CompressionMethod.DEFLATE)
            return new ZipInputStream(new InflaterInputStream(raf, offsetStartOfData, comprSize, this), this);
        throw new ZipException("compression type not supported");
    }

    private void init(RandomAccessFile raf) throws ZipException {

        if (localFileHeader == null) {
            throw new ZipException("local file header is null, cannot initialize input stream");
        }

        try {
            initDecrypter(raf);
        } catch(ZipException e) {
            throw e;
        } catch(Exception e) {
            throw new ZipException(e);
        }
    }

    private void initDecrypter(RandomAccessFile raf) throws ZipException {
        if (localFileHeader == null)
            throw new ZipException("local file header is null, cannot init decrypter");

        if (localFileHeader.getEncryption() == Encryption.STANDARD)
            decrypter = new StandardDecrypter(fileHeader, getStandardDecrypterHeaderBytes(raf));
        else if (localFileHeader.getEncryption() == Encryption.AES)
            decrypter = new AESDecrypter(localFileHeader, getAESSalt(raf), getAESPasswordVerifier(raf));
        else if (localFileHeader.getEncryption() != Encryption.OFF)
            throw new ZipException("unsupported encryption method");
    }

    private byte[] getStandardDecrypterHeaderBytes(RandomAccessFile raf) throws ZipException {
        try {
            byte[] headerBytes = new byte[InternalZipConstants.STD_DEC_HDR_SIZE];
            raf.seek(localFileHeader.getOffsetStartOfData());
            raf.read(headerBytes, 0, 12);
            return headerBytes;
        } catch(IOException e) {
            throw new ZipException(e);
        } catch(Exception e) {
            throw new ZipException(e);
        }
    }

    private byte[] getAESSalt(RandomAccessFile raf) throws ZipException {
        if (localFileHeader.getAesExtraDataRecord() == null)
            return null;

        try {
            AESExtraDataRecord aesExtraDataRecord = localFileHeader.getAesExtraDataRecord();
            byte[] saltBytes = new byte[calculateAESSaltLength(aesExtraDataRecord)];
            raf.seek(localFileHeader.getOffsetStartOfData());
            raf.read(saltBytes);
            return saltBytes;
        } catch(IOException e) {
            throw new ZipException(e);
        }
    }

    private byte[] getAESPasswordVerifier(RandomAccessFile raf) throws ZipException {
        try {
            byte[] pvBytes = new byte[2];
            raf.read(pvBytes);
            return pvBytes;
        } catch(IOException e) {
            throw new ZipException(e);
        }
    }

    private int calculateAESSaltLength(AESExtraDataRecord aesExtraDataRecord) throws ZipException {
        if (aesExtraDataRecord == null)
            throw new ZipException("unable to determine salt length: AESExtraDataRecord is null");

        if (aesExtraDataRecord.getAesStrength() == AESStrength.STRENGTH_128)
            return 8;
        if (aesExtraDataRecord.getAesStrength() == AESStrength.STRENGTH_192)
            return 12;
        if (aesExtraDataRecord.getAesStrength() == AESStrength.STRENGTH_256)
            return 16;
        throw new ZipException("unable to determine salt length: invalid aes key strength");
    }

    public void checkCRC() throws ZipException {
        if (fileHeader != null) {
            if (fileHeader.getEncryption() == Encryption.AES) {
                if (decrypter != null && decrypter instanceof AESDecrypter) {
                    byte[] tmpMacBytes = ((AESDecrypter)decrypter).getCalculatedAuthenticationBytes();
                    byte[] storedMac = ((AESDecrypter)decrypter).getStoredMac();
                    byte[] calculatedMac = new byte[InternalZipConstants.AES_AUTH_LENGTH];

                    if (calculatedMac == null || storedMac == null) {
                        throw new ZipException("CRC (MAC) check failed for " + fileHeader.getFileName());
                    }

                    System.arraycopy(tmpMacBytes, 0, calculatedMac, 0, InternalZipConstants.AES_AUTH_LENGTH);

                    if (!Arrays.equals(calculatedMac, storedMac)) {
                        throw new ZipException("invalid CRC (MAC) for file: " + fileHeader.getFileName());
                    }
                }
            } else {
                long calculatedCRC = crc.getValue() & 0xFFFFFFFFL;
                if (calculatedCRC != (fileHeader.getCrc32() & 0xFFFFFFFFL)) {
                    String errMsg = "invalid CRC for file: " + fileHeader.getFileName();
                    if (localFileHeader.getEncryption() == Encryption.STANDARD)
                        errMsg += " - Wrong Password?";
                    throw new ZipException(errMsg);
                }
            }
        }
    }

//	private void checkCRC() throws ZipException {
//		if (fileHeader != null) {
//			if (fileHeader.getEncryption() == Zip4jConstants.AES) {
//				if (decrypter != null && decrypter instanceof AESDecrypter) {
//					byte[] tmpMacBytes = ((AESDecrypter)decrypter).getCalculatedAuthenticationBytes();
//					byte[] actualMacBytes = ((AESDecrypter)decrypter).getStoredMac();
//					if (tmpMacBytes == null || actualMacBytes == null) {
//						throw new ZipException("null mac value for AES encrypted file: " + fileHeader.getFileName());
//					}
//					byte[] calcMacBytes = new byte[10];
//					System.arraycopy(tmpMacBytes, 0, calcMacBytes, 0, 10);
//					if (!Arrays.equals(calcMacBytes, actualMacBytes)) {
//						throw new ZipException("invalid CRC(mac) for file: " + fileHeader.getFileName());
//					}
//				} else {
//					throw new ZipException("invalid decryptor...cannot calculate mac value for file: "
//							+ fileHeader.getFileName());
//				}
//			} else if (unzipEngine != null) {
//				long calculatedCRC = unzipEngine.getCRC();
//				long actualCRC = fileHeader.getCrc32();
//				if (calculatedCRC != actualCRC) {
//					throw new ZipException("invalid CRC for file: " + fileHeader.getFileName());
//				}
//			}
//		}
//	}

    private boolean checkLocalHeader() throws ZipException {
        RandomAccessFile rafForLH = null;
        try {
            rafForLH = checkSplitFile();

            if (rafForLH == null)
                rafForLH = new RandomAccessFile(zipModel.getZipFile().toFile(), InternalZipConstants.READ_MODE);

            localFileHeader = new LocalFileHeaderReader(new LittleEndianRandomAccessFile(rafForLH), fileHeader).read();

            if (localFileHeader == null) {
                throw new ZipException("error reading local file header. Is this a valid zip file?");
            }

            //TODO Add more comparision later
            if (localFileHeader.getCompressionMethod() != fileHeader.getCompressionMethod()) {
                return false;
            }

            return true;
        } catch(Exception e) {
            throw new ZipException(e);
        } finally {
            if (rafForLH != null) {
                try {
                    rafForLH.close();
                } catch(IOException e) {
                    // Ignore this
                } catch(Exception e) {
                    //Ignore this
                }
            }
        }
    }

    private RandomAccessFile checkSplitFile() throws ZipException {
        if (zipModel.isSplitArchive()) {
            int diskNumberStartOfFile = fileHeader.getDiskNumberStart();
            currSplitFileCounter = diskNumberStartOfFile + 1;
            String partFile;

            if (diskNumberStartOfFile == zipModel.getEndCentralDirectory().getNoOfDisk())
                partFile = zipModel.getZipFile().toString();
            else
                partFile = ZipModel.getSplitFilePath(zipModel.getZipFile(), diskNumberStartOfFile + 1).toString();

            try {
                RandomAccessFile raf = new RandomAccessFile(partFile, InternalZipConstants.READ_MODE);

                if (currSplitFileCounter == 1) {
                    byte[] splitSig = new byte[4];
                    raf.read(splitSig);
                    if (Raw.readIntLittleEndian(splitSig, 0) != InternalZipConstants.SPLITSIG) {
                        throw new ZipException("invalid first part split file signature");
                    }
                }
                return raf;
            } catch(FileNotFoundException e) {
                throw new ZipException(e);
            } catch(IOException e) {
                throw new ZipException(e);
            }
        }
        return null;
    }

    private RandomAccessFile createFileHandler(String mode) throws ZipException {
        if (zipModel == null || zipModel.getZipFile() == null)
            throw new ZipException("input parameter is null in getFilePointer");

        try {
            return zipModel.isSplitArchive() ? checkSplitFile() : new RandomAccessFile(zipModel.getZipFile().toFile(), mode);
        } catch(FileNotFoundException e) {
            throw new ZipException(e);
        } catch(Exception e) {
            throw new ZipException(e);
        }
    }

    private FileOutputStream getOutputStream(@NonNull Path destDir) throws ZipException {
        try {
            File file = destDir.resolve(fileHeader.getFileName()).toFile();

            if (!file.getParentFile().exists())
                file.getParentFile().mkdirs();

            if (file.exists())
                file.delete();

            return new FileOutputStream(file);
        } catch(FileNotFoundException e) {
            throw new ZipException(e);
        }
    }

    public RandomAccessFile startNextSplitFile() throws IOException {
        String partFile = null;

        if (currSplitFileCounter == zipModel.getEndCentralDirectory().getNoOfDisk())
            partFile = zipModel.getZipFile().toString();
        else
            partFile = ZipModel.getSplitFilePath(zipModel.getZipFile(), currSplitFileCounter + 1).toString();

        currSplitFileCounter++;
        if (!new File(partFile).exists()) {
            throw new IOException("zip split file does not exist: " + partFile);
        }
        return new RandomAccessFile(partFile, InternalZipConstants.READ_MODE);
    }

    private void closeStreams(InputStream is, OutputStream os) throws ZipException {
        try {
            if (is != null) {
                is.close();
                is = null;
            }
        } catch(IOException e) {
            if (e != null && StringUtils.isNotBlank(e.getMessage())) {
                if (e.getMessage().indexOf(" - Wrong Password?") >= 0) {
                    throw new ZipException(e.getMessage());
                }
            }
        } finally {
            try {
                if (os != null) {
                    os.close();
                    os = null;
                }
            } catch(IOException e) {
                //do nothing
            }
        }
    }

    public void updateCRC(int b) {
        crc.update(b);
    }

    public void updateCRC(byte[] buff, int offset, int len) {
        if (buff != null) {
            crc.update(buff, offset, len);
        }
    }

    public CentralDirectory.FileHeader getFileHeader() {
        return fileHeader;
    }

    public IDecrypter getDecrypter() {
        return decrypter;
    }

    public ZipModel getZipModel() {
        return zipModel;
    }

    public LocalFileHeader getLocalFileHeader() {
        return localFileHeader;
    }

}
