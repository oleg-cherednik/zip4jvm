package ru.olegcherednik.zip4jvm.engine.decompose;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.io.readers.block.BlockModelReader;
import ru.olegcherednik.zip4jvm.model.block.BlockModel;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * @author Oleg Cherednik
 * @since 15.11.2019
 */
@RequiredArgsConstructor
public final class DecomposeEngine {

    private final Path zip;
    private final ZipInfoSettings settings;

    public void decompose(PrintStream out) throws IOException {
        boolean emptyLine = false;

        for (BaseDecompose decompose : getDecomposes(createModel()))
            emptyLine = decompose.print(out, emptyLine);
    }

    public void decompose(Path destDir) throws IOException {
        Files.createDirectories(destDir);

        for (BaseDecompose decompose : getDecomposes(createModel()))
            decompose.write(destDir);
    }

    private BlockModel createModel() throws IOException {
        BlockModelReader reader = new BlockModelReader(zip, settings.getCustomizeCharset());
        return settings.isReadEntries() ? reader.readWithEntries() : reader.read();
    }

    private List<BaseDecompose> getDecomposes(BlockModel blockModel) {
        return Arrays.asList(
                new EndCentralDirectoryDecompose(blockModel, settings),
                new Zip64Decompose(blockModel, settings),
                new CentralDirectoryDecompose(blockModel, settings),
                new ZipEntriesDecompose(blockModel, settings));
    }

}