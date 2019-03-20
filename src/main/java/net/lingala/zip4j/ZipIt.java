package net.lingala.zip4j;

import lombok.Builder;
import lombok.NonNull;
import net.lingala.zip4j.engine.ZipEngine;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.InputStreamMeta;
import net.lingala.zip4j.model.ZipModel;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.CreateZipModelSup;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
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

    public void add(@NonNull Collection<Path> paths, @NonNull ZipParameters parameters) throws ZipException, IOException {
        if (paths.stream().anyMatch(path -> !Files.isDirectory(path) && !Files.isRegularFile(path)))
            throw new ZipException("Cannot add neither directory nor regular file to zip");

        for (Path path : paths)
            add(path, parameters);
    }

    public void add(@NonNull Path path, @NonNull ZipParameters parameters) throws ZipException, IOException {
        if (Files.isDirectory(path))
            addDirectory(path, parameters);
        else if (Files.isRegularFile(path))
            addRegularFile(path, parameters);
        else
            throw new ZipException("Cannot add neither directory nor regular file to zip: " + path);
    }

    public void add(@NonNull InputStreamMeta file, @NonNull ZipParameters parameters) throws ZipException {
        addStream(Collections.singletonList(file), parameters);
    }

    public void addStream(@NonNull Collection<InputStreamMeta> files, @NonNull ZipParameters parameters) throws ZipException {
        ZipModel zipModel = new CreateZipModelSup(zipFile, charset).get();
        checkSplitArchiveModification(zipModel);
        zipModel.setSplitLength(parameters.getSplitLength());

        parameters.setSourceExternalStream(true);

        new ZipEngine(zipModel).addStreamToZip(files, parameters);
    }

    // TODO addDirectory and addRegularFile are same
    private void addDirectory(Path dir, ZipParameters parameters) throws ZipException, IOException {
        assert Files.isDirectory(dir);

        if (Files.isDirectory(dir) && parameters.getDefaultFolderPath() == null)
            parameters.setDefaultFolderPath(dir);

        ZipModel zipModel = new CreateZipModelSup(zipFile, charset).get();
        checkSplitArchiveModification(zipModel);
        zipModel.setSplitLength(parameters.getSplitLength());

        new ZipEngine(zipModel).addEntries(getDirectoryEntries(dir), parameters);
    }

    private void addRegularFile(Path file, ZipParameters parameters) throws ZipException, IOException {
        assert Files.isRegularFile(file);

        ZipModel zipModel = new CreateZipModelSup(zipFile, charset).get();
        checkSplitArchiveModification(zipModel);
        zipModel.setSplitLength(parameters.getSplitLength());

        new ZipEngine(zipModel).addEntries(Collections.singletonList(file), parameters);
    }

    static void checkSplitArchiveModification(@NonNull ZipModel zipModel) throws ZipException {
        if (Files.exists(zipModel.getZipFile()) && zipModel.isSplitArchive())
            throw new ZipException("Zip file already exists. Zip file format does not allow updating split/spanned files");
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
