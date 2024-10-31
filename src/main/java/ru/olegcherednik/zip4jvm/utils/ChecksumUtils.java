package ru.olegcherednik.zip4jvm.utils;

import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.codec.digest.PureJavaCrc32;
import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.CheckedInputStream;
import java.util.zip.Checksum;

/**
 * @author Oleg Cherednik
 * @since 31.10.2024
 */
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public final class ChecksumUtils {

    @SuppressWarnings("NewMethodNamingConvention")
    public static long crc32(Path file) {
        return Quietly.doQuietly(() -> {
            Checksum crc32 = new PureJavaCrc32();

            try (CheckedInputStream in = new CheckedInputStream(
                    new BufferedInputStream(Files.newInputStream(file)), crc32)) {
                byte[] buf = new byte[1024];

                while (in.read(buf) != IOUtils.EOF) {
                    // read file in completely
                }
            }

            return crc32.getValue();
        });
    }

}
