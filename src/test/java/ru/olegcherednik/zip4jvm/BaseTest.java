package ru.olegcherednik.zip4jvm;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 16.08.2026
 */
public abstract class BaseTest {

    protected final DirRoot dirRoot = new DirRoot();

    @BeforeClass
    public void createDir() {
        dirRoot.createDir();
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public void removeDir() {
        dirRoot.removeDir();
    }

    public Path getZip() {
        return dirRoot.getZipSrc();
    }

}
