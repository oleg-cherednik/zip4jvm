package ru.olegcherednik.zip4jvm.io.in.file;

import lombok.Builder;
import lombok.Getter;

import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 24.01.2020
 */
@Getter
@Builder
public final class Disk {

    private final int num;
    private final Path file;
    private final long offs;
    private final long length;

    @Override
    public String toString() {
        return String.format("%s (offs: %s)", file.toString(), offs);
    }
}
