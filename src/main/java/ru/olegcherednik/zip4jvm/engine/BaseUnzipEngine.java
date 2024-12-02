package ru.olegcherednik.zip4jvm.engine;

import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.out.decorators.UncloseableDataOutput;
import ru.olegcherednik.zip4jvm.model.AesVersion;
import ru.olegcherednik.zip4jvm.model.Charsets;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.model.password.PasswordProvider;
import ru.olegcherednik.zip4jvm.utils.ZipUtils;
import ru.olegcherednik.zip4jvm.utils.time.DosTimestampConverterUtils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.PureJavaCrc32;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ChecksumInputStream;

import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 25.11.2024
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseUnzipEngine {

    protected final PasswordProvider passwordProvider;

    protected void extractEntry(Path destDir,
                                ZipEntry zipEntry,
                                Function<ZipEntry, String> getFileName) throws IOException {
        Path file = destDir.resolve(getFileName.apply(zipEntry));

        if (zipEntry.isSymlink())
            extractSymlink(file, zipEntry);
        else if (zipEntry.isDirectory())
            extractEmptyDirectory(file);
        else
            extractRegularFile(file, zipEntry);

        // TODO attributes for directory should be set at the end (under Posix, it could have less privelegies)
        setFileAttributes(file, zipEntry);
        setFileLastModifiedTime(file, zipEntry);
    }

    private static void extractSymlink(Path symlink, ZipEntry zipEntry) throws IOException {
        String target = IOUtils.toString(zipEntry.createInputStream(), Charsets.UTF_8);

        if (target.startsWith("/"))
            ZipSymlinkEngine.createAbsoluteSymlink(symlink, Paths.get(target));
        else if (target.contains(":"))
            // TODO absolute windows symlink
            throw new Zip4jvmException("windows absolute symlink not supported");
        else
            ZipSymlinkEngine.createRelativeSymlink(symlink, symlink.getParent().resolve(target));
    }

    private static void extractEmptyDirectory(Path dir) throws IOException {
        Files.createDirectories(dir);
    }

    @SuppressWarnings("PMD.CloseResource")
    private void extractRegularFile(Path file, ZipEntry zipEntry) throws IOException {
        String fileName = ZipUtils.getFileNameNoDirectoryMarker(zipEntry.getFileName());
        zipEntry.setPassword(passwordProvider.getFilePassword(fileName));

        InputStream in = new FilterInputStream(zipEntry.createInputStream()) {
            @Override
            public void close() throws IOException {
                in.close();
            }

            @Override
            public String toString() {
                return "Filter 1";
            }
        };

        if (zipEntry.getAesVersion() != AesVersion.AE_2) {
            in = ChecksumInputStream.builder()
                                    .setExpectedChecksumValue(zipEntry.getChecksum())
                                    .setChecksum(new PureJavaCrc32())
                                    .setInputStream(in)
                                    .get();
        }

        in = new FilterInputStream(in) {
            @Override
            public void close() throws IOException {
                in.close();
            }

            @Override
            public String toString() {
                return "Filter 2";
            }
        };
        ZipUtils.copyLarge(in, getOutputStream(file));
    }

    private static void setFileLastModifiedTime(Path path, ZipEntry zipEntry) {
        try {
            long lastModifiedTime = DosTimestampConverterUtils.dosToJavaTime(zipEntry.getLastModifiedTime());
            Files.setLastModifiedTime(path, FileTime.fromMillis(lastModifiedTime));
        } catch (IOException ignored) {
        }
    }

    private static void setFileAttributes(Path path, ZipEntry zipEntry) {
        try {
            if (zipEntry.getExternalFileAttributes() != null)
                zipEntry.getExternalFileAttributes().apply(path);
        } catch (IOException ignored) {
        }
    }

    private static OutputStream getOutputStream(Path file) throws IOException {
        Path parent = file.getParent();

        if (!Files.exists(parent))
            Files.createDirectories(parent);

        Files.deleteIfExists(file);
        return Files.newOutputStream(file);
    }

}
