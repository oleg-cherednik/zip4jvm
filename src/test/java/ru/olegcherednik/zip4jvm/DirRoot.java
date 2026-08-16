package ru.olegcherednik.zip4jvm;

import java.nio.file.Path;

import static ru.olegcherednik.zip4jvm.TestData.fileNameZipSrc;

/**
 * @author Oleg Cherednik
 * @since 16.08.2026
 */
public class DirRoot {

    final Path path = Zip4jvmSuite.generateSubDirNameWithTime();

    public void createDir() {
        Zip4jvmSuite.createDir(path);
    }

    public void removeDir() {
        Zip4jvmSuite.removeDir(path);
    }

    public Path getZipSrc() {
        return Zip4jvmSuite.subDirNameAsMethodName(path).resolve(fileNameZipSrc);
    }

}
