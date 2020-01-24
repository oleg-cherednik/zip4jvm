package ru.olegcherednik.zip4jvm.io.in;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 20.01.2020
 */
@Getter
@RequiredArgsConstructor
public final class SingleZip extends Zip {

    private final Path path;

    @Override
    public Path getDiskPath(int disk) {
        return path;
    }

    @Override
    public long getTotalDisks() {
        return 0;
    }

    @Override
    public long length() throws IOException {
        return Files.size(path);
    }
}
