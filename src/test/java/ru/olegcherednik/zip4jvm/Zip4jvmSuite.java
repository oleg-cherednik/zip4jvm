/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.lingala.zip4j.ZipFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import ru.olegcherednik.zip4jvm.data.DefalteZipData;
import ru.olegcherednik.zip4jvm.data.StoreZipData;
import ru.olegcherednik.zip4jvm.model.Charsets;
import ru.olegcherednik.zip4jvm.model.password.PasswordProvider;
import ru.olegcherednik.zip4jvm.utils.ZipUtils;
import ru.olegcherednik.zip4jvm.view.View;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.olegcherednik.zip4jvm.TestData.dirEmpty;
import static ru.olegcherednik.zip4jvm.TestData.dirRoot;
import static ru.olegcherednik.zip4jvm.TestData.dirSrc;
import static ru.olegcherednik.zip4jvm.TestDataAssert.rootAssert;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatDirectory;

/**
 * @author Oleg Cherednik
 * @since 23.03.2019
 */
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

    private static final long time = System.currentTimeMillis();
    private static final String timeStr = new SimpleDateFormat("yyyyMMdd_HHmmss").format(time);

    @BeforeSuite
    public void beforeSuite() throws IOException {
        removeDir(dirRoot);

        copyTestData();
        StoreZipData.createStoreZip();
        DefalteZipData.createDeflateZip();
    }

    @AfterSuite(enabled = clear)
    public void afterSuite() throws IOException {
        removeDir(dirRoot);
    }

    private static void copyTestData() throws IOException {
        Files.createDirectories(dirEmpty);

        Path dataDir = Paths.get("src/test/resources/data").toAbsolutePath();

        Files.walk(dataDir).forEach(path -> {
            try {
                if (Files.isDirectory(path)) Files.createDirectories(dirSrc.resolve(dataDir.relativize(path)));
                else if (Files.isRegularFile(path)) Files.copy(path, dirSrc.resolve(dataDir.relativize(path)));
            } catch(IOException e) {
                e.printStackTrace();
            }
        });

        assertThatDirectory(dirSrc).matches(rootAssert);
    }

    public static void removeDir(Path path) throws IOException {
        if (Files.exists(path)) FileUtils.deleteQuietly(path.toFile());
    }

    public static Path copy(Path destDir, Path zip) throws IOException {
        if (new ZipFile(zip.toFile()).isSplitArchive()) {
            final String fileName = FilenameUtils.getBaseName(zip.getFileName().toString());

            List<Path> parts = Files.walk(zip.getParent()).filter(Files::isRegularFile).filter(
                    path -> FilenameUtils.getBaseName(path.getFileName().toString()).equals(fileName)).collect(Collectors.toList());

            for (Path part : parts)
                Files.copy(part, destDir.resolve(part.getFileName()));

        } else Files.copy(zip, destDir.resolve(zip.getFileName()));

        return destDir.resolve(zip.getFileName());
    }

    public static Path generateSubDirName(Class<?> cls) {
        return dirRoot.resolve(cls.getSimpleName());
    }

    public static Path generateSubDirNameWithTime(Class<?> cls) {
        String baseDir = Zip4jvmSuite.class.getPackage().getName();
        String[] parts = cls.getName().substring(baseDir.length() + 1).split("\\.");
        Path path = dirRoot;

        if (parts.length == 1) path = path.resolve(parts[0]).resolve(timeStr);
        else {
            for (int i = 0; i < parts.length; i++) {
                if (i == 1) path = path.resolve(timeStr);

                path = path.resolve(parts[i]);
            }
        }

        return path;
    }

    public static Path temp() {
        return dirRoot.resolve("tmp");
    }

    public static Path temporaryFile(String ext) {
        return temp().resolve(UUID.randomUUID().toString() + '.' + ext);
    }

    public static Path subDirNameAsMethodNameWithTime(Path rootDir) {
        return rootDir.resolve(TestDataAssert.getMethodName()).resolve(Paths.get(timeStr));
    }

    public static Path subDirNameAsMethodName(Path rootDir) throws IOException {
        return Files.createDirectories(rootDir.resolve(TestDataAssert.getMethodName()));
    }

    public static Path subDirNameAsRelativePathToRoot(Path rootDir, Path zipFile) {
        Path path;

        zipFile = zipFile.toAbsolutePath();

        if (zipFile.toString().contains("resources")) {
            Path parent = zipFile.getParent();

            while (!"resources".equalsIgnoreCase(parent.getFileName().toString()) && !"resources".equalsIgnoreCase(
                    parent.getParent().getFileName().toString())) {
                parent = parent.getParent();
            }

            path = parent.relativize(zipFile);
        } else path = dirRoot.relativize(zipFile);

        String dirName = path.toString().replaceAll("\\\\", "_");

        return rootDir.resolve(dirName);
    }

    public static String[] execute(View view) throws IOException {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream(); PrintStream out = new PrintStream(os, true, Charsets.UTF_8.name())) {
            assertThat(view.printTextInfo(out)).isTrue();
            return new String(os.toByteArray(), Charsets.UTF_8).split(System.lineSeparator());
        }
    }

    public static Set<String> getResourceFiles(String name) throws IOException {
        Path parent = new File(Zip4jvmSuite.class.getResource(name).getPath()).toPath();

        return Files.walk(parent).filter(path -> Files.isRegularFile(path)).map(
                path -> ZipUtils.normalizeFileName(parent.relativize(path).toString())).collect(Collectors.toSet());
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
        public char[] getCentralDirectoryPassword() {
            return null;
        }
    }

}
