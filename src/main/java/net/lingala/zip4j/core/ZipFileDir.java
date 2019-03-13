package net.lingala.zip4j.core;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
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
 * @since 13.03.2019
 */
@Setter
@Getter
@RequiredArgsConstructor
public final class ZipFileDir {

    @NonNull
    private final Path path;
    @NonNull
    private Charset charset = Charset.defaultCharset();

    private ZipModel zipModel;

    public void addFolder(@NonNull Path dir, @NonNull ZipParameters parameters) throws ZipException, IOException {
        dir = Files.isRegularFile(dir) ? dir.getParent() : dir;
        parameters.setDefaultFolderPath(dir.toString());

        // TODO check cannot add new files to exited split archive
        zipModel = ZipFile.readOrCreateModel(zipModel, path, charset);
        zipModel.setSplitLength(parameters.getSplitLength());
        new ZipEngine(zipModel).addFiles(getDirectoryEntries(dir), parameters);
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
