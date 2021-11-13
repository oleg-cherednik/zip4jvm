package ru.olegcherednik.zip4jvm.compatibility;

import com.github.luben.zstd.Zstd;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Oleg Cherednik
 * @since 13.11.2021
 */
public class Bar {

    public static void main(String... args) throws IOException {
        Path file = Paths.get("src/test/resources/data/Oleg Cherednik.txt").toAbsolutePath();
        byte[] src = Files.readAllBytes(file);
        byte[] dest = Zstd.compress(src);
        Files.write(Paths.get("d:/oleg-cherednik.zip"), dest);
    }

}
