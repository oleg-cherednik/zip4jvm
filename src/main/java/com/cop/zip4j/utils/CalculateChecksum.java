package com.cop.zip4j.utils;

import com.cop.zip4j.exception.ZipException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.LongSupplier;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

/**
 * @author Oleg Cherednik
 * @since 07.03.2019
 */
@RequiredArgsConstructor
public final class CalculateChecksum implements LongSupplier {

    @NonNull
    private final Path file;

    @Override
    public long getAsLong() {
        try (CheckedInputStream in = new CheckedInputStream(new FileInputStream(file.toFile()), new CRC32())) {
            byte[] buf = new byte[128];

            while (in.read(buf) >= 0) {
            }

            return in.getChecksum().getValue();
        } catch(IOException e) {
            throw new ZipException(e);
        }
    }
}
