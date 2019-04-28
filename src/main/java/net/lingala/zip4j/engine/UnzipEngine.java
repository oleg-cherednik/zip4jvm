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
import net.lingala.zip4j.crypto.AesDecoder;
import net.lingala.zip4j.crypto.Decoder;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.InflaterInputStream;
import net.lingala.zip4j.io.PartInputStream;
import net.lingala.zip4j.io.ZipInputStream;
import net.lingala.zip4j.model.CentralDirectory;
import net.lingala.zip4j.model.CompressionMethod;
import net.lingala.zip4j.model.Encryption;
import net.lingala.zip4j.model.LocalFileHeader;
import net.lingala.zip4j.model.ZipModel;
import net.lingala.zip4j.utils.InternalZipConstants;
import net.lingala.zip4j.io.LittleEndianRandomAccessFile;
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
import java.util.function.Function;
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
    private Decoder decoder;

    private final Function<CentralDirectory.FileHeader, CentralDirectory.FileHeader> setPassword =
            new Function<CentralDirectory.FileHeader, CentralDirectory.FileHeader>() {
                @Override
                public CentralDirectory.FileHeader apply(CentralDirectory.FileHeader fileHeader) {
                    if (fileHeader.isEncrypted())
                        fileHeader.setPassword(password);
                    return fileHeader;
                }
            };

    public void extractEntries(@NonNull Path destDir, @NonNull Collection<String> entries) {
        getFileHeaders(entries).forEach(fileHeader -> extractEntry(destDir, fileHeader));
    }

    private List<CentralDirectory.FileHeader> getFileHeaders(Collection<String> entries) {
        return entries.stream()
                      .map(entryName -> zipModel.getCentralDirectory().getFileHeadersByPrefix(entryName))
                      .flatMap(List::stream)
                      .filter(Objects::nonNull)
                      .map(setPassword)
                      .collect(Collectors.toList());
    }

    private void extractEntry(Path destDir, @NonNull CentralDirectory.FileHeader fileHeader) {
        crc.reset();

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
        return extractEntryAsStream(zipModel.getCentralDirectory().getFileHeaderByEntryName(entryName));
    }

    @NonNull
    private InputStream extractEntryAsStream(@NonNull CentralDirectory.FileHeader fileHeader) {
        try {
            if (fileHeader.isEncrypted())
                fileHeader.setPassword(password);

            // TODO temporary
            this.fileHeader = fileHeader;

            LittleEndianRandomAccessFile in = openFile(fileHeader);
            LocalFileHeader localFileHeader = readLocalFileHeader(fileHeader);
            this.localFileHeader = localFileHeader;
            decoder = localFileHeader.getEncryption().decoder(in, fileHeader, localFileHeader);

            long comprSize = localFileHeader.getCompressedSize();
            long offs = localFileHeader.getOffs();

            if (localFileHeader.getEncryption() == Encryption.AES) {
                if (decoder instanceof AesDecoder) {
                    comprSize -= ((AesDecoder)decoder).getSaltLength() +
                            ((AesDecoder)decoder).getPasswordVerifierLength() + 10;
                    offs += ((AesDecoder)decoder).getSaltLength() +
                            ((AesDecoder)decoder).getPasswordVerifierLength();
                } else
                    throw new ZipException("invalid decryptor when trying to calculate " +
                            "compressed size for AES encrypted file: " + fileHeader.getFileName());
            } else if (localFileHeader.getEncryption() == Encryption.STANDARD) {
                // TODO decrypter throws unsupported exception
                comprSize -= InternalZipConstants.STD_DEC_HDR_SIZE;
                offs += InternalZipConstants.STD_DEC_HDR_SIZE;
            }

            in.seek(offs);

            if (fileHeader.getActualCompressionMethod() == CompressionMethod.STORE)
                return new ZipInputStream(new PartInputStream(in, comprSize, this), this);
            if (fileHeader.getActualCompressionMethod() == CompressionMethod.DEFLATE)
                return new ZipInputStream(new InflaterInputStream(in, comprSize, this), this);

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
                if (decoder != null && decoder instanceof AesDecoder) {
                    byte[] tmpMacBytes = ((AesDecoder)decoder).getCalculatedAuthenticationBytes();
                    byte[] storedMac = ((AesDecoder)decoder).getStoredMac();
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

    @NonNull
    private LocalFileHeader readLocalFileHeader(@NonNull CentralDirectory.FileHeader fileHeader) throws IOException {
        try (LittleEndianRandomAccessFile in = openFile(fileHeader)) {
            return new LocalFileHeaderReader(fileHeader).read(in);
        }
    }

    private LittleEndianRandomAccessFile openFile(@NonNull CentralDirectory.FileHeader fileHeader) throws IOException {
        if (!zipModel.isSplitArchive())
            return new LittleEndianRandomAccessFile(zipModel.getZipFile());

        int diskNumber = fileHeader.getDiskNumber();
        currSplitFileCounter = diskNumber + 1;

        LittleEndianRandomAccessFile in = new LittleEndianRandomAccessFile(zipModel.getPartFile(diskNumber));

        if (currSplitFileCounter == 1) {
            int signature = in.readDword();

            if (signature != InternalZipConstants.SPLITSIG)
                throw new ZipException("Expected first part of split file signature (offs:" + in.getFilePointer() + ')');
        }

        return in;
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

        if (currSplitFileCounter == zipModel.getEndCentralDirectory().getSplitParts())
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

    public Decoder getDecoder() {
        return decoder;
    }

    public ZipModel getZipModel() {
        return zipModel;
    }

}
