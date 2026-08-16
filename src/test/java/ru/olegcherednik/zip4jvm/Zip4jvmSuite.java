/*
 * Copyright 2019 Oleg Cherednik (oleg.cherednik@gmail.com)
 *
 * Licensed under The Apache Software License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package ru.olegcherednik.zip4jvm;

import ru.olegcherednik.zip4jvm.data.DeflateZipData;
import ru.olegcherednik.zip4jvm.data.StoreZipData;
import ru.olegcherednik.zip4jvm.data.SymlinkData;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.model.charset.Charsets;
import ru.olegcherednik.zip4jvm.model.password.PasswordProvider;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;
import ru.olegcherednik.zip4jvm.view.View;
import ru.olegcherednik.zip4jvm.view.out.Out;
import ru.olegcherednik.zip4jvm.view.out.PrintStreamOut;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.ZipFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.stream.Stream;

import static ru.olegcherednik.zip4jvm.TestData.dirEmpty;
import static ru.olegcherednik.zip4jvm.TestData.dirRoot;
import static ru.olegcherednik.zip4jvm.TestData.dirSrc;
import static ru.olegcherednik.zip4jvm.TestData.dirSrcData;
import static ru.olegcherednik.zip4jvm.TestData.dirSrcTemp;
import static ru.olegcherednik.zip4jvm.TestData.dirTime;
import static ru.olegcherednik.zip4jvm.TestDataAssert.rootAssert;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatDirectory;

/**
 * @author Oleg Cherednik
 * @since 23.03.2019
 */
@Slf4j
@SuppressWarnings("FieldNamingConvention")
public class Zip4jvmSuite {

    /** Password for encrypted zip */
    public static final String passwordStr = "1";
    public static final char[] password = passwordStr.toCharArray();
    public static final PasswordProvider fileNamePasswordProvider = new FileNamePasswordProvider();
    /** Clear resources */
    public static final boolean clear = false;

    public static final long SIZE_1MB = 1024 * 1024;
    public static final long SIZE_2MB = 2 * SIZE_1MB;

    static {
        createDir(dirTime);
    }

    @BeforeSuite
    public void beforeSuite() {
        removeDir(dirSrc);

        copyTestData();
        StoreZipData.createStoreZip();
        DeflateZipData.createDeflateZip();
        SymlinkData.createSymlinkData();
    }

    @AfterSuite(enabled = clear)
    public void afterSuite() {
        removeDir(dirRoot);
    }

    private static void copyTestData() {
        createDir(dirEmpty);

        Path dataDir = Paths.get("src/test/resources/data").toAbsolutePath();

        try (Stream<Path> dirs = Files.walk(dataDir)) {
            dirs.forEach(path -> {
                if (Files.isDirectory(path))
                    createDir(dirSrcData.resolve(dataDir.relativize(path)));
                else if (Files.isRegularFile(path))
                    copyFile(path, dirSrcData.resolve(dataDir.relativize(path)));
            });
        } catch (IOException e) {
            throw new Zip4jvmException(e);
        }

        assertThatDirectory(dirSrcData).matches(rootAssert);
    }

    public static void copyToDir(Path src, Path dstDir) {
        assert !Files.isSymbolicLink(src) : "src should not be a symlink: " + src;

        createDir(dstDir);

        if (Files.isDirectory(src))
            Quietly.doRuntime(() -> FileUtils.copyDirectory(src.toFile(), dstDir.toFile()));
        else
            copyFile(src, dstDir.resolve(src.getFileName().toString()));
    }

    public static Path createDir(Path path) {
        return Quietly.doRuntime(() -> Files.createDirectories(path));
    }

    public static void removeDir(Path path) {
        if (Files.exists(path))
            FileUtils.deleteQuietly(path.toFile());
    }

    public static Path copyFile(Path source, Path target, CopyOption... options) {
        return Quietly.doRuntime(() -> Files.copy(source, target, options));
    }

    public static Path copy(Path dstDir, Path zip) {
        boolean split;

        try (ZipFile zipFile = new ZipFile(zip.toFile())) {
            split = zipFile.isSplitArchive();
        } catch (IOException e) {
            throw new Zip4jvmException(e);
        }

        if (split) {
            final String fileName = FilenameUtils.getBaseName(zip.getFileName().toString());

            try (Stream<Path> dirs = Files.walk(zip.getParent())) {
                dirs.filter(Files::isRegularFile).filter(
                            path -> FilenameUtils.getBaseName(path.getFileName().toString()).equals(fileName))
                    .forEach(part -> Quietly.doRuntime(() -> copyAndReplace(dstDir, part)));
            } catch (IOException e) {
                throw new Zip4jvmException(e);
            }
        } else
            copyAndReplace(dstDir, zip);

        return dstDir.resolve(zip.getFileName());
    }

    private static void copyAndReplace(Path dstDir, Path zip) {
        Path dstZip = dstDir.resolve(zip.getFileName());
        FileUtils.deleteQuietly(dstZip.toFile());
        copyFile(zip, dstDir.resolve(zip.getFileName()));
    }

    public static Path generateSubDirNameWithTime() {
        Class<?> cls = CallerInfo.getCallerClass();
        String baseDir = Zip4jvmSuite.class.getPackage().getName();
        String[] parts = cls.getName().substring(baseDir.length() + 1).split("\\.");
        Path path = dirTime;

        for (String part : parts)
            path = path.resolve(part);

        return path;
    }

    public static Path temporaryFile(String ext) {
        return dirSrcTemp.resolve(UUID.randomUUID() + "." + ext);
    }

    public static Path subDirNameAsMethodName(Path rootDir) {
        String methodName = CallerInfo.getCallerMethodName();
        return createDir(rootDir.resolve(methodName));
    }

    @SuppressWarnings("DynamicRegexReplaceableByCompiledPattern")
    public static Path subDirNameAsRelativePathToRoot(Path rootDir, Path zipFile) {
        Path path;

        zipFile = zipFile.toAbsolutePath();

        if (zipFile.toString().contains("resources")) {
            Path parent = zipFile.getParent();

            while (!"resources".equalsIgnoreCase(parent.getFileName().toString())
                    && !"resources".equalsIgnoreCase(parent.getParent().getFileName().toString())) {
                parent = parent.getParent();
            }

            path = parent.relativize(zipFile);
        } else
            path = dirTime.relativize(zipFile);

        String dirName = path.toString().replaceAll("\\\\", "_");

        return rootDir.resolve(dirName);
    }

    public static String[] execute(View view) {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream();
             Out out = new PrintStreamOut(new PrintStream(os, true, Charsets.UTF_8.name()))) {
            view.printTextInfo(out);
            return new String(os.toByteArray(), Charsets.UTF_8).split(System.lineSeparator());
        } catch (IOException e) {
            throw new Zip4jvmException(e);
        }
    }

    public static String[] executeNew(View view) {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream();
             Out out = new PrintStreamOut(new PrintStream(os, true, Charsets.UTF_8.name()))) {
            view.printTextInfo(out);
            return new String(os.toByteArray(), Charsets.UTF_8).split(System.lineSeparator());
        } catch (IOException e) {
            throw new Zip4jvmException(e);
        }
    }

    public static Path getResourcePath(String name) {
        return Paths.get("src/test/resources", name).toAbsolutePath();
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class FileNamePasswordProvider implements PasswordProvider {

        @Override
        public char[] getFilePassword(String fileName) {
            return fileName.toCharArray();
        }

        @Override
        @SuppressWarnings("PMD.ReturnEmptyCollectionRatherThanNull")
        public char[] getCentralDirectoryPassword() {
            return null;
        }
    }

}
