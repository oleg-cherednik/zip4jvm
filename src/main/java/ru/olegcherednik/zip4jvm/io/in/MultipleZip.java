package ru.olegcherednik.zip4jvm.io.in;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 20.01.2020
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MultipleZip implements Zip {

    private final Path path;
    private final int total;

    public static MultipleZip create(Path zip) {
        Path parent = zip.getParent();
        String fileName = zip.getFileName().toString();

        if ("001".equals(FilenameUtils.getExtension(fileName)))
            fileName = fileName.substring(0, fileName.length() - 4);

        int total = 0;

        while (Files.exists(parent.resolve(String.format("%s.%03d", fileName, total + 1))))
            total++;

        return new MultipleZip(parent.resolve(fileName), total);
    }
}
