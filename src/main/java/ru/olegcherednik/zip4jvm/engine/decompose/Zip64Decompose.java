package ru.olegcherednik.zip4jvm.engine.decompose;

import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.block.BlockModel;
import ru.olegcherednik.zip4jvm.model.block.Zip64Block;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;
import ru.olegcherednik.zip4jvm.view.Zip64View;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 06.12.2019
 */
final class Zip64Decompose extends BaseDecompose {

    public Zip64Decompose(BlockModel blockModel, ZipInfoSettings settings) {
        super(blockModel, settings);
    }

    @Override
    public Zip64View createView() {
        return Zip64View.builder()
                        .zip64(blockModel.getZip64())
                        .block(blockModel.getZip64Block())
                        .position(settings.getOffs(), settings.getColumnWidth()).build();
    }

    @Override
    public void write(Path destDir) throws IOException {
        if (blockModel.getZip64() == Zip64.NULL)
            return;

        Path dir = destDir.resolve("zip64");
        Files.createDirectories(dir);

        // (PK0607) ZIP64 End of Central directory locator
        try (PrintStream out = new PrintStream(dir.resolve("zip64_end_central_directory_locator.txt").toFile())) {
            createZip64EndCentralDirectoryLocatorView(blockModel.getZip64(), blockModel.getZip64Block()).print(out);
        }

        copyLarge(blockModel.getZipModel().getFile(), dir.resolve("zip64_end_central_directory_locator.data"),
                blockModel.getZip64Block().getEndCentralDirectoryLocatorBlock());

        // (PK0606) ZIP64 End of Central directory record
        try (PrintStream out = new PrintStream(dir.resolve("zip64_end_central_directory.txt").toFile())) {
            createZip64EndCentralDirectoryView(blockModel.getZip64(), blockModel.getZip64Block()).print(out);
        }

        copyLarge(blockModel.getZipModel().getFile(), dir.resolve("zip64_end_central_directory.data"),
                blockModel.getZip64Block().getEndCentralDirectoryBlock());
    }

    private Zip64View.EndCentralDirectoryLocatorView createZip64EndCentralDirectoryLocatorView(Zip64 zip64, Zip64Block block) {
        return Zip64View.EndCentralDirectoryLocatorView.builder()
                                                       .locator(zip64.getEndCentralDirectoryLocator())
                                                       .block(block.getEndCentralDirectoryLocatorBlock())
                                                       .position(settings.getOffs(), settings.getColumnWidth()).build();
    }

    private Zip64View.EndCentralDirectoryView createZip64EndCentralDirectoryView(Zip64 zip64, Zip64Block block) {
        return Zip64View.EndCentralDirectoryView.builder()
                                                .endCentralDirectory(zip64.getEndCentralDirectory())
                                                .block(block.getEndCentralDirectoryBlock())
                                                .position(settings.getOffs(), settings.getColumnWidth()).build();
    }
}
