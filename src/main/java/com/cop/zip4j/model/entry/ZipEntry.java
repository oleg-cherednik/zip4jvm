package com.cop.zip4j.model.entry;

import com.cop.zip4j.exception.Zip4jException;
import com.cop.zip4j.model.Compression;
import com.cop.zip4j.model.CompressionLevel;
import com.cop.zip4j.model.Encryption;
import com.cop.zip4j.model.ExternalFileAttributes;
import com.cop.zip4j.model.ZipModel;
import com.cop.zip4j.model.ZipParameters;
import com.cop.zip4j.utils.ZipUtils;
import com.cop.zip4j.utils.function.IOSupplier;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 26.07.2019
 */
public abstract class ZipEntry {

    // TODO should be ZipEntry
    public static PathZipEntry of(Path path, ZipParameters parameters) {
        if (Files.isDirectory(path)) {
            try {
                String fileName = parameters.getRelativeEntryName(path);
                int lastModifiedTime = ZipUtils.javaToDosTime(Files.getLastModifiedTime(path).toMillis());
                ExternalFileAttributes attributes = ExternalFileAttributes.createOperationBasedDelegate();
                attributes.readFrom(path);
                return apply(new DirectoryZipEntry(fileName, lastModifiedTime, attributes), parameters, path);
            } catch(IOException e) {
                throw new Zip4jException(e);
            }
        }

        if (Files.isRegularFile(path)) {
            try {
                String fileName = parameters.getRelativeEntryName(path);
                long uncompressedSize = Files.size(path);
                int lastModifiedTime = ZipUtils.javaToDosTime(Files.getLastModifiedTime(path).toMillis());
                Compression compression = parameters.getCompression();
                CompressionLevel compressionLevel = parameters.getCompressionLevel();
                Encryption encryption = parameters.getEncryption();
                boolean zip64 = parameters.isZip64() || uncompressedSize > ZipModel.MAX_ENTRY_SIZE;
                ExternalFileAttributes attributes = ExternalFileAttributes.createOperationBasedDelegate();
                attributes.readFrom(path);
                IOSupplier<InputStream> inputStream = () -> new FileInputStream(path.toFile());
                return apply(new RegularFileZipEntry(fileName, uncompressedSize, lastModifiedTime, compression, compressionLevel, encryption, zip64,
                        attributes, inputStream), parameters, path);
            } catch(IOException e) {
                throw new Zip4jException(e);
            }
        }

        throw new Zip4jException("Cannot add neither directory nor regular file to zip");
    }

    private static PathZipEntry apply(PathZipEntry zipEntry, ZipParameters parameters, Path path) {
        zipEntry.setPassword(parameters.getPassword());
        return zipEntry;
    }

}
