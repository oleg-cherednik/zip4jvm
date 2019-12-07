package ru.olegcherednik.zip4jvm.engine.decompose;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.engine.decompose.centraldirectory.CentralDirectoryDecompose;
import ru.olegcherednik.zip4jvm.io.readers.block.BlockModelReader;
import ru.olegcherednik.zip4jvm.model.block.BlockModel;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 15.11.2019
 */
@RequiredArgsConstructor
public final class DecomposeEngine {

    private final Path zip;
    private final ZipInfoSettings settings;

    public void decompose(PrintStream out) throws IOException {
        BlockModel blockModel = createModel();

        boolean emptyLine = new EndCentralDirectoryDecompose(blockModel, settings).print(out, false);
        emptyLine = new Zip64Decompose(blockModel, settings).print(out, emptyLine);
        emptyLine = new CentralDirectoryDecompose(blockModel, settings).print(out, emptyLine);
        new ZipEntriesDecompose(blockModel, settings).print(out, emptyLine);
    }

    public void decompose(Path destDir) throws IOException {
        Files.createDirectories(destDir);

        BlockModel blockModel = createModel();

        new EndCentralDirectoryDecompose(blockModel, settings).write(destDir);
        new Zip64Decompose(blockModel, settings).write(destDir);
        new CentralDirectoryDecompose(blockModel, settings).write(destDir);
        new ZipEntriesDecompose(blockModel, settings).write(destDir);
    }

    private BlockModel createModel() throws IOException {
        BlockModelReader reader = new BlockModelReader(zip, settings.getCustomizeCharset());
        return settings.isReadEntries() ? reader.readWithEntries() : reader.read();
    }

}
