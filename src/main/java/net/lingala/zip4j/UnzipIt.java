package net.lingala.zip4j;

import lombok.Builder;
import lombok.NonNull;
import net.lingala.zip4j.core.ZipFileUnzip;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.UnzipParameters;

import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 14.03.2019
 */
@Builder
public class UnzipIt {

    @NonNull
    private final Path zipFile;

    public void extract(@NonNull Path destDir, @NonNull UnzipParameters parameters) throws ZipException {
        new ZipFileUnzip(zipFile).extract(destDir, parameters);
    }
}
