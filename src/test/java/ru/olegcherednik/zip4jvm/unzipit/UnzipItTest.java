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

    public void shouldUnzipRecursivelyMaxWhenMaxLevel() {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
        UnzipIt.zip(Zip4jvmSuite.getResourcePath("zip/recursive.zip"))
               .settings(UnzipSettings.builder()
                                      .asyncOff()
                                      .recursiveLevel(4)
                                      .build())
               .dstDir(dstDir).extract();
        int a = 0;
        a++;
    }

}
