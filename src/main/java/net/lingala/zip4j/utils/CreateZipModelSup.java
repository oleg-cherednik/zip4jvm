package net.lingala.zip4j.utils;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.core.readers.ZipModelReader;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipModel;

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
public final class CreateZipModelSup implements Supplier<ZipModel> {
    @NonNull
    private final Path zipFile;
    @NonNull
    private final Charset charset;

    @NonNull
    @Override
    public ZipModel get() {
        try {
            if (Files.exists(zipFile))
                return new ZipModelReader(zipFile, charset).read();

            return new ZipModel(zipFile, charset);
        } catch(IOException e) {
            throw new ZipException(e);
        }
    }
}
