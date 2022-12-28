package ru.olegcherednik.zip4jvm.view.decompose;

import ru.olegcherednik.zip4jvm.model.block.BlockModel;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 25.12.2022
 */
public class EncryptedCentralDirectoryDecompose extends CentralDirectoryDecompose {

    public EncryptedCentralDirectoryDecompose(BlockModel blockModel, ZipInfoSettings settings) {
        super(blockModel, settings);
    }

    @Override
    public void decompose(Path dir) throws IOException {
        super.decompose(dir);
    }
}
