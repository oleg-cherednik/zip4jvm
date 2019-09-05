package ru.olegcherednik.zip4jvm;

import lombok.NonNull;
import ru.olegcherednik.zip4jvm.engine.ZipEngine;
import ru.olegcherednik.zip4jvm.io.out.DataOutput;
import ru.olegcherednik.zip4jvm.model.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.ZipParameters;
import ru.olegcherednik.zip4jvm.model.builders.ZipModelBuilder;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

/**
 * ZipFile real-time implementation.
 * <br>
 * When create new instance of this class:
 * <ul>
 * <li><i>zip file exists</i> - open zip archive</li>
 * <li><i>zip file not exists</i> - create new empty zip archive</li>
 * </ul>
 * <p>
 * To close zip archive correctly, do call {@link ZipFile#close()} method.
 * <pre>
 * try (ZipFile zipFile = new ZipFile(Paths.get("~/src.zip"))) {
 *     zipFile.addEntry(...);
 * }
 * </pre>
 *
 * @author Oleg Cherednik
 * @since 01.09.2019
 */
public final class ZipFile implements Closeable {

    private final Charset charset = StandardCharsets.UTF_8;
    private final ZipModel zipModel;
    private final DataOutput out;

    public ZipFile(@NonNull Path file) throws IOException {
        this(file, ZipParameters.builder().build());
    }

    public ZipFile(@NonNull Path file, @NonNull ZipParameters parameters) throws IOException {
        zipModel = ZipModelBuilder.readOrCreate(file, charset).noSplitOnly();
        out = ZipEngine.createDataOutput(zipModel);
        out.seek(zipModel.getCentralDirectoryOffs());
    }

    public void add(@NonNull Path path, ZipEntrySettings settings) {
        List<Path> paths = Collections.singletonList(path);
        List<ZipEntry> entries = ZipIt.createEntries(ZipIt.withExistedEntries(paths), settings);

        // TODO throw exception if duplication found
        entries.stream()
               .filter(entry -> !entry.isRoot())
               .forEach(entry -> ZipEngine.writeEntry(entry, out, zipModel));
    }

    @Override
    public void close() throws IOException {
        // TODO check for zip64
        out.close();
    }
}
