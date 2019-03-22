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
import net.lingala.zip4j.crypto.Decrypter;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.InflaterInputStream;
import net.lingala.zip4j.io.PartInputStream;
import net.lingala.zip4j.io.ZipInputStream;
import net.lingala.zip4j.model.CentralDirectory;
import net.lingala.zip4j.model.CompressionMethod;
import net.lingala.zip4j.model.Encryption;
import net.lingala.zip4j.model.LocalFileHeader;
import net.lingala.zip4j.model.ZipModel;
import net.lingala.zip4j.util.InternalZipConstants;
import net.lingala.zip4j.util.LittleEndianRandomAccessFile;
import org.apache.commons.io.IOUtils;

import java.io.File;
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
    private final char[] password;
    private final CRC32 crc = new CRC32();

    @Deprecated
    private CentralDirectory.FileHeader fileHeader;
    @Deprecated
    private LocalFileHeader localFileHeader;

    private int currSplitFileCounter;
    private Decrypter decrypter;

    public void extractEntries(@NonNull Path destDir, @NonNull Collection<String> entries) {
        getFileHeaders(entries).forEach(fileHeader -> extractEntry(destDir, fileHeader));
    }

    private List<CentralDirectory.FileHeader> getFileHeaders(Collection<String> entries) {
        List<CentralDirectory.FileHeader> fileHeaders = entries.stream()
                                                               .map(entryName -> zipModel.getCentralDirectory().getFileHeadersByPrefix(entryName))
                                                               .flatMap(List::stream)
                                                               .filter(Objects::nonNull)
                                                               .collect(Collectors.toList());

        fileHeaders.stream()
                   .filter(CentralDirectory.FileHeader::isEncrypted)
                   .forEach(fileHeader -> fileHeader.setPassword(password));

        return fileHeaders;
    }

    private void extractEntry(Path destDir, @NonNull CentralDirectory.FileHeader fileHeader) {
        crc.reset();
        this.fileHeader = fileHeader;

        if (fileHeader.isDirectory())
            try {
                Files.createDirectories(destDir.resolve(fileHeader.getFileName()));
            } catch(IOException e) {
                throw new ZipException(e);
            }
        else {
            try (InputStream in = extractEntryAsStream(fileHeader); OutputStream out = getOutputStream(destDir, fileHeader)) {
                IOUtils.copyLarge(in, out);
            } catch(IOException e) {
                throw new ZipException(e);
            }
        }
    }

    @NonNull
    public InputStream extractEntry(@NonNull String entryName) {
        CentralDirectory.FileHeader fileHeader = zipModel.getCentralDirectory().getFileHeaderByEntryName(entryName);
        // TODO temporary
        this.fileHeader = fileHeader;

        if (fileHeader.isEncrypted())
            fileHeader.setPassword(password);

        return extractEntryAsStream(fileHeader);
    }

    @NonNull
    private InputStream extractEntryAsStream(@NonNull CentralDirectory.FileHeader fileHeader) {
        try {
            LittleEndianRandomAccessFile in = openFile(fileHeader);
            LocalFileHeader localFileHeader = checkLocalHeader(fileHeader);
            this.localFileHeader = localFileHeader;
            decrypter = localFileHeader.getEncryption().createDecrypter(in, fileHeader, localFileHeader);

            long comprSize = localFileHeader.getCompressedSize();
            long offs = localFileHeader.getOffsetStartOfData();

            if (localFileHeader.getEncryption() == Encryption.AES) {
                if (decrypter instanceof AESDecrypter) {
                    comprSize -= ((AESDecrypter)decrypter).getSaltLength() +
                            ((AESDecrypter)decrypter).getPasswordVerifierLength() + 10;
                    offs += ((AESDecrypter)decrypter).getSaltLength() +
                            ((AESDecrypter)decrypter).getPasswordVerifierLength();
                } else {
                    throw new ZipException("invalid decryptor when trying to calculate " +
                            "compressed size for AES encrypted file: " + fileHeader.getFileName());
                }
            } else if (localFileHeader.getEncryption() == Encryption.STANDARD) {
                // TODO decrypter throws unsupported exception
                comprSize -= InternalZipConstants.STD_DEC_HDR_SIZE;
                offs += InternalZipConstants.STD_DEC_HDR_SIZE;
            }

            CompressionMethod compressionMethod = fileHeader.getCompressionMethod();
            if (fileHeader.getEncryption() == Encryption.AES) {
                if (fileHeader.getAesExtraDataRecord() != null)
                    compressionMethod = fileHeader.getAesExtraDataRecord().getCompressionMethod();
                else
                    throw new ZipException("AESExtraDataRecord does not exist for AES encrypted file: " + fileHeader.getFileName());
            }

            in.seek(offs);

            if (compressionMethod == CompressionMethod.STORE)
                return new ZipInputStream(new PartInputStream(in.getRaf(), comprSize, this), this);
            if (compressionMethod == CompressionMethod.DEFLATE)
                return new ZipInputStream(new InflaterInputStream(in.getRaf(), comprSize, this), this);

            throw new ZipException("compression type not supported");
        } catch(ZipException e) {
            throw e;
        } catch(Exception e) {
            throw new ZipException(e);
        }
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

    private LocalFileHeader checkLocalHeader(@NonNull CentralDirectory.FileHeader fileHeader) throws IOException {
        try (LittleEndianRandomAccessFile in = openFile(fileHeader)) {
            LocalFileHeader localFileHeader = new LocalFileHeaderReader(in, fileHeader).read();

            //TODO Add more comparision later
            if (localFileHeader.getCompressionMethod() != fileHeader.getCompressionMethod())
                throw new ZipException("local header and file header do not match");

            return localFileHeader;
        }
    }

    private LittleEndianRandomAccessFile openFile(@NonNull CentralDirectory.FileHeader fileHeader) {
        try {
            if (!zipModel.isSplitArchive())
                return new LittleEndianRandomAccessFile(zipModel.getZipFile());

            int diskNumber = fileHeader.getDiskNumber();
            currSplitFileCounter = diskNumber + 1;

            LittleEndianRandomAccessFile in = new LittleEndianRandomAccessFile(zipModel.getPartFile(diskNumber));

            if (currSplitFileCounter == 1) {
                int signature = in.readInt();

                if (signature != InternalZipConstants.SPLITSIG)
                    throw new IOException("Expected first part of split file signature (offs:" + in.getFilePointer() + ')');
            }

            return in;
        } catch(IOException e) {
            throw new ZipException(e);
        }
    }

    private static FileOutputStream getOutputStream(@NonNull Path destDir, @NonNull CentralDirectory.FileHeader fileHeader) {
        try {
            Path file = destDir.resolve(fileHeader.getFileName());
            Path parent = file.getParent();

            if (!Files.exists(file))
                Files.createDirectories(parent);

            Files.deleteIfExists(file);

            return new FileOutputStream(file.toFile());
        } catch(IOException e) {
            throw new ZipException(e);
        }
    }

    public RandomAccessFile startNextSplitFile() throws IOException {
        String partFile;

        if (currSplitFileCounter == zipModel.getEndCentralDirectory().getDiskNumber())
            partFile = zipModel.getZipFile().toString();
        else
            partFile = ZipModel.getSplitFilePath(zipModel.getZipFile(), currSplitFileCounter + 1).toString();

        currSplitFileCounter++;
        if (!new File(partFile).exists()) {
            throw new IOException("zip split file does not exist: " + partFile);
        }
        return new RandomAccessFile(partFile, "r");
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

    public Decrypter getDecrypter() {
        return decrypter;
    }

    public ZipModel getZipModel() {
        return zipModel;
    }

}
