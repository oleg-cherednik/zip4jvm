package ru.olegcherednik.zip4jvm.io.in;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 20.01.2020
 */
@Getter
@RequiredArgsConstructor
public final class SingleZip implements Zip {

    private final Path path;
}
