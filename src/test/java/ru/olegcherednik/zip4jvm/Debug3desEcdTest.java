package ru.olegcherednik.zip4jvm;

import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static ru.olegcherednik.zip4jvm.TestData.secureZipStoreSolid3Des168StrongEcdZip;
import static ru.olegcherednik.zip4jvm.TestData.secureZipStoreSolidAes256StrongEcdZip;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.password;

@Test
@SuppressWarnings("NewClassNamingConvention")
public class Debug3desEcdTest {

    private static final Path DIR_ROOT = Zip4jvmSuite.generateSubDirNameWithTime();

    public void debug() throws IOException {
        dump("AES256", secureZipStoreSolidAes256StrongEcdZip);
        dump("3DES168", secureZipStoreSolid3Des168StrongEcdZip);
    }

    private static void dump(String label, Path zip) throws IOException {
        System.out.println("=== " + label + " ===");
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve(label);
        try {
            UnzipIt.zip(zip).dstDir(dstDir).password(password).extract();
        } catch (Exception e) {
            System.out.println("  EXTRACT THREW: " + e);
        }
        if (Files.exists(dstDir))
            try (Stream<Path> walk = Files.walk(dstDir)) {
                walk.filter(Files::isRegularFile)
                    .forEach(p -> {
                        try {
                            System.out.println("  " + dstDir.relativize(p) + "  bytes=" + Files.size(p));
                        } catch (IOException ignore) {
                        }
                    });
            }
    }

}
