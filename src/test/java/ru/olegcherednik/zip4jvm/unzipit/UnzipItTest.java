package ru.olegcherednik.zip4jvm.unzipit;

import ru.olegcherednik.zip4jvm.UnzipIt;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.model.settings.UnzipSettings;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 13.09.2025
 */
@Test
public class UnzipItTest {

    private static final Path DIR_ROOT = Zip4jvmSuite.generateSubDirNameWithTime();

    @BeforeClass
    public static void createDir() {
        Zip4jvmSuite.createDir(DIR_ROOT);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() {
        Zip4jvmSuite.removeDir(DIR_ROOT);
    }

    public void shouldUnzipRecursiveOffWhenDefaultSettings() {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
        Path zip = Zip4jvmSuite.getResourcePath("zip/recursive.zip");
        UnzipIt.zip(zip).dstDir(dstDir).extract();
    }

    public void shouldUnzipRecursiveMaxWhenMaxLevel() {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
        Path zip = Zip4jvmSuite.getResourcePath("zip/recursive.zip");
        UnzipIt.zip(zip)
               .settings(UnzipSettings.builder().recursiveLevelMax().build())
               .dstDir(dstDir).extract();
    }

    public void shouldNotUnzipGroupZipWhenMaxLevelNotEnough() {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
        Path zip = Zip4jvmSuite.getResourcePath("zip/recursive.zip");
        UnzipIt.zip(zip)
               .settings(UnzipSettings.builder().recursiveLevel(2).build())
               .dstDir(dstDir).extract();
    }

    public void shouldUnzipNotMoreThanGroupZipWhenMaxLevelForGroup() {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
        Path zip = Zip4jvmSuite.getResourcePath("zip/recursive.zip");
        UnzipIt.zip(zip)
               .settings(UnzipSettings.builder().recursiveLevel(3).build())
               .dstDir(dstDir).extract();
    }

}
