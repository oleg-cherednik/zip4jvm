package net.lingala.zip4j;

import lombok.Builder;
import lombok.NonNull;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipModel;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.zip.ZipEngine;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Oleg Cherednik
 * @since 14.03.2019
 */
@Builder
public final class ZipIt {
    @NonNull
    private final Path zipFile;
    @NonNull
    @Builder.Default
    private final Charset charset = Charset.defaultCharset();

    public void add(@NonNull Path path, @NonNull ZipParameters parameters) throws ZipException, IOException {
        if (Files.isDirectory(path))
            addDirectory(path, parameters);
    }

    private void addDirectory(Path dir, ZipParameters parameters) throws ZipException, IOException {
        assert Files.isDirectory(dir);

        dir = Files.isRegularFile(dir) ? dir.getParent() : dir;
        parameters.setDefaultFolderPath(dir.toString());

        // TODO check cannot add new files to exited split archive
        ZipModel zipModel = ZipFile.createZipModel(zipFile, charset);
        zipModel.setSplitLength(parameters.getSplitLength());
        new ZipEngine(zipModel).addEntries(getDirectoryEntries(dir), parameters);
    }

    @NonNull
    private static List<Path> getDirectoryEntries(@NonNull Path dir) {
        try {
            return Files.walk(dir)
                        .filter(path -> Files.isRegularFile(path) || Files.isDirectory(path))
                        .collect(Collectors.toList());
        } catch(IOException e) {
            return Collections.emptyList();
        }
    }
}
