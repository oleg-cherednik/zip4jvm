package com.cop.zip4j.utils;

import com.cop.zip4j.core.readers.ZipModelReader;
import com.cop.zip4j.exception.ZipException;
import com.cop.zip4j.model.ZipModel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

/**
 * @author Oleg Cherednik
 * @since 20.03.2019
 */
@RequiredArgsConstructor
public final class CreateZipModel implements Supplier<ZipModel> {

    @NonNull
    private final Path zipFile;
    @NonNull
    private final Charset charset;

    @NonNull
    @Override
    public ZipModel get() {
        try {
            return Files.exists(zipFile) ? new ZipModelReader(zipFile, charset).read() : new ZipModel(zipFile, charset);
        } catch(IOException e) {
            throw new ZipException(e);
        }
    }
}
