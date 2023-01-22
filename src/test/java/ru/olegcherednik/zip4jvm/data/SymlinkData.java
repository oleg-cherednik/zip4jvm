package ru.olegcherednik.zip4jvm.data;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static ru.olegcherednik.zip4jvm.TestData.dirSrcSymlink;
import static ru.olegcherednik.zip4jvm.TestData.fileDucati;
import static ru.olegcherednik.zip4jvm.TestData.symlinkRelativeDucati;

/**
 * @author Oleg Cherednik
 * @since 22.01.2023
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SymlinkData {

    public static void createSymlinkData() throws IOException {
        Path destFileDucati = dirSrcSymlink.resolve(fileDucati.getFileName());
        Files.createDirectories(destFileDucati.getParent());
        Files.copy(fileDucati, destFileDucati);

        Files.createSymbolicLink(symlinkRelativeDucati, symlinkRelativeDucati.getParent().relativize(destFileDucati));
        int a = 0;
        a++;
//        Files.copy(fileDucati, dirSrcSymlink.resolve(fileDucati.getFileName()));
//        Path dataDir = Paths.get("src/test/resources/symlink").toAbsolutePath();
//
//        Files.walk(dataDir).forEach(path -> {
//            try {
//                if (Files.isDirectory(path))
//                    Files.createDirectories(dirSrcSymlink.resolve(dataDir.relativize(path)));
//                else if (Files.isRegularFile(path))
//                    Files.copy(path, dirSrcSymlink.resolve(dataDir.relativize(path)));
//            } catch(IOException e) {
//                e.printStackTrace();
//            }
//        });

//        assertThatDirectory(dirSrcSymlink).matches(rootAssert);
    }

}
