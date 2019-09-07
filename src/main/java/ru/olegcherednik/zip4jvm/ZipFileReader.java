package ru.olegcherednik.zip4jvm;

import lombok.NonNull;
import ru.olegcherednik.zip4jvm.engine.UnzipEngine;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.builders.ZipModelBuilder;
import ru.olegcherednik.zip4jvm.model.settings.ZipFileReadSettings;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 07.09.2019
 */
public class ZipFileReader {

    private final ZipModel zipModel;
    private final ZipFileReadSettings settings;

    public ZipFileReader(Path zip, ZipFileReadSettings settings) throws IOException {
        zipModel = ZipModelBuilder.read(zip);
        this.settings = settings;
    }

    public void extract(@NonNull Path destDir) {
        zipModel.getEntries().forEach(entry -> entry.setPassword(settings.getPassword()));
        new UnzipEngine(zipModel, settings.getPassword()).extractEntries(destDir, zipModel.getEntryNames());
    }

//    private List<ZipEntry> getEntries(@NonNull Collection<String> entrieNames) {
//        return entrieNames.parallelStream()
//                      .map(entrieName -> zipModel.getEntryNames()name -> zipModel.getEntries().stream()
//                                       .filter(entry -> entry.getFileName().toLowerCase().startsWith(name))
//                                       .collect(Collectors.toList()))
//                      .flatMap(List::stream)
//                      .filter(Objects::nonNull)
//                      .collect(Collectors.toList());
//    }

}
