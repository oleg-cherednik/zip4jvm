package ru.olegcherednik.zip4jvm.engine.decompose;

import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;
import ru.olegcherednik.zip4jvm.view.IView;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 07.12.2019
 */
public final class ExtraFieldDecompose extends BaseDecompose {

    public ExtraFieldDecompose(ZipModel zipModel, ZipInfoSettings settings) {
        super(zipModel, settings);
    }

    @Override
    protected IView createView() {
        return null;
    }

    @Override
    public void write(Path destDir) throws IOException {

    }

}
