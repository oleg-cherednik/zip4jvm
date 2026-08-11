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
package ru.olegcherednik.zip4jvm.utils;

import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.utils.function.InputStreamSupplier;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Oleg Cherednik
 * @since 06.08.2026
 */
@Test
public class PathUtilsTest {

    private static final Path DIR_ROOT = Zip4jvmSuite.generateSubDirNameWithTime();

    private static final String ONE_TXT = "one.txt";
    private static final String CONTENT = "zip4jvm";

    @BeforeClass
    public static void createDir() {
        Zip4jvmSuite.createDir(DIR_ROOT);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() {
        Zip4jvmSuite.removeDir(DIR_ROOT);
    }

    // ---------- getName ----------

    public void shouldRetrieveFileNameWhenGetName() {
        assertThat(PathUtils.getName(Paths.get("one", "two", ONE_TXT))).isEqualTo(ONE_TXT);
        assertThat(PathUtils.getName(Paths.get(ONE_TXT))).isEqualTo(ONE_TXT);
    }

    // ---------- getOffsStr ----------

    public void shouldRetrieveFormattedStringWhenGetOffsStr() {
        assertThat(PathUtils.getOffsStr(0)).isEqualTo("offs: 0 (0x0)");
        assertThat(PathUtils.getOffsStr(255)).isEqualTo("offs: 255 (0xff)");
        assertThat(PathUtils.getOffsStr(4096)).isEqualTo("offs: 4096 (0x1000)");
    }

    public void shouldRetrieveFormattedStringWhenGetOffsStrForSplitZip() {
        assertThat(PathUtils.getOffsStr(255, 16, 2))
                .isEqualTo("absOffs: 255 (0xff) | diskOffs: 16 (0x10) | disk: 2");
    }

    // ---------- size ----------

    public void shouldRetrieveFileSizeWhenSize() throws IOException {
        Path file = createFile(Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve(ONE_TXT), CONTENT);
        assertThat(PathUtils.size(file)).isEqualTo(CONTENT.length());
    }

    public void shouldThrowExceptionWhenSizeForNotExistedFile() {
        Path file = DIR_ROOT.resolve("not_existed_file.txt");
        assertThatThrownBy(() -> PathUtils.size(file)).isInstanceOf(Zip4jvmException.class);
    }

    // ---------- list ----------

    public void shouldRetrieveAllChildrenWhenList() throws IOException {
        Path dir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
        createFile(dir.resolve(ONE_TXT), CONTENT);
        createFile(dir.resolve("two.txt"), CONTENT);
        Files.createDirectories(dir.resolve("sub"));

        List<Path> children = PathUtils.list(dir);

        assertThat(children).hasSize(3);
        assertThat(children.stream().map(PathUtils::getName)).containsExactlyInAnyOrder(ONE_TXT, "two.txt", "sub");
    }

    public void shouldRetrieveEmptyListWhenListForEmptyDir() throws IOException {
        Path dir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
        Files.createDirectories(dir);
        assertThat(PathUtils.list(dir)).isEmpty();
    }

    // ---------- newInputStream / newOutputStream ----------

    public void shouldReadContentWhenNewInputStream() throws IOException {
        Path file = createFile(Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve(ONE_TXT), CONTENT);

        try (InputStream in = PathUtils.newInputStream(file)) {
            assertThat(toString(in)).isEqualTo(CONTENT);
        }
    }

    public void shouldWriteContentWhenNewOutputStream() throws IOException {
        Path dir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
        Files.createDirectories(dir);
        Path file = dir.resolve(ONE_TXT);

        try (OutputStream out = PathUtils.newOutputStream(file)) {
            out.write(CONTENT.getBytes(StandardCharsets.UTF_8));
        }

        assertThat(readContent(file)).isEqualTo(CONTENT);
    }

    public void shouldRetrieveSizeAndStreamWhenNewInputStreamSupplier() throws IOException {
        Path file = createFile(Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve(ONE_TXT), CONTENT);
        InputStreamSupplier supplier = PathUtils.newInputStreamSupplier(file);

        assertThat(supplier.getSize()).isEqualTo(CONTENT.length());

        try (InputStream in = supplier.get()) {
            assertThat(toString(in)).isEqualTo(CONTENT);
        }
    }

    // ---------- deleteIfExists ----------

    public void shouldRetrieveTrueWhenDeleteIfExistsForExistedFile() throws IOException {
        Path file = createFile(Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve(ONE_TXT), CONTENT);

        assertThat(PathUtils.deleteIfExists(file)).isTrue();
        assertThat(Files.exists(file)).isFalse();
    }

    public void shouldRetrieveFalseWhenDeleteIfExistsForNotExistedFile() {
        assertThat(PathUtils.deleteIfExists(DIR_ROOT.resolve("not_existed_file.txt"))).isFalse();
    }

    // ---------- createDirectories ----------

    public void shouldCreateAllParentDirectoriesWhenCreateDirectories() {
        Path dir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve("one/two/three");

        assertThat(PathUtils.createDirectories(dir)).isEqualTo(dir);
        assertThat(Files.isDirectory(dir)).isTrue();
    }

    public void shouldNotThrowExceptionWhenCreateDirectoriesForExistedDir() throws IOException {
        Path dir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
        Files.createDirectories(dir);

        assertThatCode(() -> PathUtils.createDirectories(dir)).doesNotThrowAnyException();
    }

    // ---------- move ----------

    public void shouldMoveFileWhenMove() throws IOException {
        Path dir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
        Path src = createFile(dir.resolve(ONE_TXT), CONTENT);
        Path dst = dir.resolve("two.txt");

        assertThat(PathUtils.move(src, dst)).isEqualTo(dst);
        assertThat(Files.exists(src)).isFalse();
        assertThat(readContent(dst)).isEqualTo(CONTENT);
    }

    // ---------- setLastModifiedTime ----------

    public void shouldUpdateLastModifiedTimeWhenSetLastModifiedTime() throws IOException {
        Path file = createFile(Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve(ONE_TXT), CONTENT);
        FileTime time = FileTime.fromMillis(1_000_000_000_000L);

        PathUtils.setLastModifiedTime(file, time);

        assertThat(Files.getLastModifiedTime(file).toMillis()).isEqualTo(time.toMillis());
    }

    // ---------- copyByteArray ----------

    public void shouldWriteByteArrayWhenCopyByteArray() throws IOException {
        Path dir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
        Files.createDirectories(dir);
        Path file = dir.resolve(ONE_TXT);

        PathUtils.copyByteArray(file, CONTENT.getBytes(StandardCharsets.UTF_8));

        assertThat(readContent(file)).isEqualTo(CONTENT);
    }

    // ---------- isUnder ----------

    public void shouldRetrieveTrueWhenDstDirIsUnderBasePath() throws IOException {
        Path base = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
        Files.createDirectories(base);

        assertThat(PathUtils.isUnder(base, base.resolve(ONE_TXT))).isTrue();
        assertThat(PathUtils.isUnder(base, base.resolve("one/two/three.txt"))).isTrue();
        // the path is not required to exist
        assertThat(PathUtils.isUnder(base, base.resolve("not/existed/at/all.txt"))).isTrue();
    }

    public void shouldRetrieveTrueWhenDstDirIsBasePathItself() throws IOException {
        Path base = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
        Files.createDirectories(base);

        assertThat(PathUtils.isUnder(base, base)).isTrue();
        assertThat(PathUtils.isUnder(base, base.resolve("."))).isTrue();
    }

    public void shouldResolveDstDirAgainstBasePathWhenDstDirIsRelative() throws IOException {
        Path base = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
        Files.createDirectories(base);

        assertThat(PathUtils.isUnder(base, Paths.get(ONE_TXT))).isTrue();
        assertThat(PathUtils.isUnder(base, Paths.get("one/two.txt"))).isTrue();
        assertThat(PathUtils.isUnder(base, Paths.get("../one.txt"))).isFalse();
        assertThat(PathUtils.isUnder(base, Paths.get("one/../../two.txt"))).isFalse();
    }

    /**
     * The check should be done on the path element basis, i.e. a plain string prefix check is not enough.
     */
    public void shouldRetrieveFalseWhenDstDirIsSiblingWithSameNamePrefix() throws IOException {
        Path dir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
        Path base = dir.resolve("bar");
        Files.createDirectories(base);

        assertThat(PathUtils.isUnder(base, dir.resolve("bar2"))).isFalse();
        assertThat(PathUtils.isUnder(base, dir.resolve("bar2/one.txt"))).isFalse();
    }

    /**
     * This is the CVE-2007-4559 check: an entry name containing {@code ..} should not be able to step outside of the
     * destination directory.
     */
    public void shouldRetrieveFalseWhenDstDirIsAboveBasePath() throws IOException {
        Path base = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
        Files.createDirectories(base);

        assertThat(PathUtils.isUnder(base, base.resolve("../evil.txt"))).isFalse();
        assertThat(PathUtils.isUnder(base, base.resolve("../../evil.txt"))).isFalse();
        assertThat(PathUtils.isUnder(base, base.resolve("one/../../evil.txt"))).isFalse();
        assertThat(PathUtils.isUnder(base, base.resolve("one/two/../../../../evil.txt"))).isFalse();
    }

    public void shouldRetrieveFalseWhenDstDirIsAbsolutePathOutsideBasePath() throws IOException {
        Path dir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
        Path base = dir.resolve("base");
        Files.createDirectories(base);

        assertThat(PathUtils.isUnder(base, dir.resolve("outside").toAbsolutePath())).isFalse();
        assertThat(PathUtils.isUnder(base, dir.toAbsolutePath())).isFalse();
    }

    public void shouldRetrieveTrueWhenDstDirIsAbsolutePathInsideBasePath() throws IOException {
        Path base = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
        Files.createDirectories(base);

        assertThat(PathUtils.isUnder(base, base.resolve(ONE_TXT).toAbsolutePath())).isTrue();
    }

    // ---------- constructor ----------

    public void shouldRetrievePrivateConstructorWhenUtilityClass() throws NoSuchMethodException {
        Constructor<PathUtils> constructor = PathUtils.class.getDeclaredConstructor();
        assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();
    }

    // ---------- static ----------

    private static Path createFile(Path file, String content) throws IOException {
        Files.createDirectories(file.getParent());
        Files.write(file, content.getBytes(StandardCharsets.UTF_8));
        return file;
    }

    private static String readContent(Path file) throws IOException {
        return new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
    }

    private static String toString(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int len = in.read(buf);

        while (len > 0) {
            out.write(buf, 0, len);
            len = in.read(buf);
        }

        return new String(out.toByteArray(), StandardCharsets.UTF_8);
    }

}
