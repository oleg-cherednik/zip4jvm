package ru.olegcherednik.zip4jvm.io.in;

import lombok.Builder;
import lombok.Getter;

import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 24.01.2020
 */
@Getter
@Builder
public final class DiskInfo {

    private final int disk;
    private final Path file;
    private final long offs;
    private final long size;

}
