package net.lingala.zip4j.core;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.Context;
import net.lingala.zip4j.model.ZipParameters;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * @author Oleg Cherednik
 * @since 02.03.2019
 */
@RequiredArgsConstructor
public class ZipFileNew {
    private final Path zipFile;

    public void addFiles(@NonNull List<Path> files) throws ZipException, IOException {
        addFiles(files, Context.builder().build());
    }

    public void addFiles(@NonNull List<Path> files, @NonNull Context context) throws ZipException, IOException {
        ZipFile zipFile = new ZipFile(this.zipFile);

        ZipParameters parameters = new ZipParameters();
        parameters.setCompressionMethod(context.getCompression().getVal());
        parameters.setDefaultFolderPath(context.getRoot().toString());

        zipFile.addFiles(files, parameters);
    }
}
