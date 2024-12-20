package ru.olegcherednik.zip4jvm.engine;

import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.model.Charsets;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.model.password.PasswordProvider;
import ru.olegcherednik.zip4jvm.utils.ZipUtils;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;
import ru.olegcherednik.zip4jvm.utils.time.DosTimestampConverterUtils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;

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
            extractSymlink(file, zipEntry, null);
        else if (zipEntry.isDirectory())
            extractEmptyDirectory(file);
        else
            extractRegularFile(file, zipEntry);

        // TODO attributes for directory should be set at the end (under Posix, it could have less privelegies)
        setFileAttributes(file, zipEntry);
        setFileLastModifiedTime(file, zipEntry);
    }

    protected void extractEntry1(Path destDir,
                                 ZipEntry zipEntry,
                                 DataInput in,
                                 Function<ZipEntry, String> getFileName) throws IOException {
        Path file = destDir.resolve(getFileName.apply(zipEntry));

        if (zipEntry.isSymlink())
            extractSymlink(file, zipEntry, in);
        else if (zipEntry.isDirectory())
            extractEmptyDirectory(file);
        else
            extractRegularFile(file, zipEntry, in);

        // TODO attributes for directory should be set at the end (under Posix, it could have less privelegies)
        setFileAttributes(file, zipEntry);
        setFileLastModifiedTime(file, zipEntry);
    }

    private static void extractSymlink(Path symlink, ZipEntry zipEntry, DataInput in) throws IOException {
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

        InputStream in = zipEntry.createInputStream();

//        if (zipEntry.getAesVersion() != AesVersion.AE_2) {
//            in = ChecksumInputStream.builder()
//                                    .setExpectedChecksumValue(zipEntry.getChecksum())
//                                    .setChecksum(new PureJavaCrc32())
//                                    .setInputStream(in)
//                                    .get();
//        }

        ZipUtils.copyLarge(in, getOutputStream(file));
    }

    @SuppressWarnings("PMD.CloseResource")
    private void extractRegularFile(Path file, ZipEntry zipEntry, DataInput di) throws IOException {
        String fileName = ZipUtils.getFileNameNoDirectoryMarker(zipEntry.getFileName());
        zipEntry.setPassword(passwordProvider.getFilePassword(fileName));

        InputStream in = zipEntry.createInputStream(di);

//        if (zipEntry.getAesVersion() != AesVersion.AE_2) {
//            in = ChecksumInputStream.builder()
//                                    .setExpectedChecksumValue(zipEntry.getChecksum())
//                                    .setChecksum(new PureJavaCrc32())
//                                    .setInputStream(in)
//                                    .get();
//        }

        ZipUtils.copyLarge(in, getOutputStream(file));
    }

    private static void setFileLastModifiedTime(Path path, ZipEntry zipEntry) {
        Quietly.doQuietly(() -> {
            long lastModifiedTime = DosTimestampConverterUtils.dosToJavaTime(zipEntry.getLastModifiedTime());
            Files.setLastModifiedTime(path, FileTime.fromMillis(lastModifiedTime));
        });
    }

    private static void setFileAttributes(Path path, ZipEntry zipEntry) {
        Quietly.doQuietly(() -> {
            if (zipEntry.getExternalFileAttributes() != null)
                zipEntry.getExternalFileAttributes().apply(path);
        });
    }

    private static OutputStream getOutputStream(Path file) throws IOException {
        Path parent = file.getParent();

        if (!Files.exists(parent))
            Files.createDirectories(parent);

        Files.deleteIfExists(file);
        return Files.newOutputStream(file);
    }

}
