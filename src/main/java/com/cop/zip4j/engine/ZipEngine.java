package com.cop.zip4j.engine;

import com.cop.zip4j.exception.ZipException;
import com.cop.zip4j.io.DeflateOutputStream;
import com.cop.zip4j.io.SplitOutputStream;
import com.cop.zip4j.model.AesStrength;
import com.cop.zip4j.model.CompressionMethod;
import com.cop.zip4j.model.Encryption;
import com.cop.zip4j.model.ZipModel;
import com.cop.zip4j.model.ZipParameters;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

/**
 * @author Oleg Cherednik
 * @since 17.03.2019
 */
@RequiredArgsConstructor
public class ZipEngine {

    @NonNull
    private final ZipModel zipModel;

    public void addEntries(@NonNull Collection<Path> entries, @NonNull ZipParameters parameters) {
        if (entries.isEmpty())
            return;

        checkParameters(parameters);

        try (DeflateOutputStream out = new DeflateOutputStream(SplitOutputStream.create(zipModel), zipModel)) {
            out.seek(zipModel.getOffsCentralDirectory());

            for (Path entry : entries) {
                if (entry == null)
                    continue;

                String entryName = parameters.getRelativeEntryName(entry);

                // TODO ignore root (it should be done prior)
                // TODO here could be empty directory ignored (also prior)
                if ("/".equals(entryName) || "\\".equals(entryName))
                    continue;

                ZipParameters params = parameters.toBuilder().build();

                if (Files.isRegularFile(entry)) {
                    params.setCrc32(FileUtils.checksumCRC32(entry.toFile()));
                    params.setCompressionMethod(Files.size(entry) == 0 ? CompressionMethod.STORE : params.getCompressionMethod());
                }

                out.putNextEntry(entry, params);

                if (Files.isRegularFile(entry))
                    copyLarge(entry, out);

                out.closeEntry();
            }

            out.finish();
        } catch(IOException e) {
            throw new ZipException(e);
        }
    }

    private static long copyLarge(@NonNull Path entry, @NonNull OutputStream out) throws IOException {
        try (InputStream in = new FileInputStream(entry.toFile())) {
            return IOUtils.copyLarge(in, out);
        }
    }

    private static void checkParameters(ZipParameters parameters) {
        if ((parameters.getCompressionMethod() != CompressionMethod.STORE) && parameters.getCompressionMethod() != CompressionMethod.DEFLATE)
            throw new ZipException("unsupported compression type");

        if (parameters.getEncryption() != Encryption.OFF) {
            if (parameters.getEncryption() != Encryption.STANDARD && parameters.getEncryption() != Encryption.AES)
                throw new ZipException("unsupported encryption method");
            if (ArrayUtils.isEmpty(parameters.getPassword()))
                throw new ZipException("input password is empty or null");
        } else {
            parameters.setAesStrength(AesStrength.NONE);
            parameters.setEncryption(Encryption.OFF);
        }
    }

}
