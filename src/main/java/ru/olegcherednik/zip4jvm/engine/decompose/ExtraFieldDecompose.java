package ru.olegcherednik.zip4jvm.engine.decompose;

import ru.olegcherednik.zip4jvm.model.ExtraField;
import ru.olegcherednik.zip4jvm.model.GeneralPurposeFlag;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.block.ExtraFieldBlock;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;
import ru.olegcherednik.zip4jvm.view.extrafield.ExtraFieldRecordView;
import ru.olegcherednik.zip4jvm.view.extrafield.ExtraFieldView;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 07.12.2019
 */
public class ExtraFieldDecompose {

    private final ZipModel zipModel;
    private final ZipInfoSettings settings;
    private final ExtraField extraField;
    private final ExtraFieldBlock block;
    private final GeneralPurposeFlag generalPurposeFlag;

    public ExtraFieldDecompose(ZipModel zipModel, ZipInfoSettings settings, ExtraField extraField, ExtraFieldBlock block,
            GeneralPurposeFlag generalPurposeFlag) {
        this.zipModel = zipModel;
        this.settings = settings;
        this.extraField = extraField;
        this.block = block;
        this.generalPurposeFlag = generalPurposeFlag;
    }

    public void write(Path destDir) throws IOException {
        destDir = destDir.resolve("extra_fields");
        Files.createDirectories(destDir);

        ExtraFieldView view = createView();

        for (int signature : extraField.getSignatures()) {
            ExtraFieldRecordView<?> recordView = view.getView(extraField.getRecord(signature));
            String fileName = recordView.getFileName();

            Utils.print(destDir.resolve(fileName + ".txt"), recordView::print);
            Utils.copyLarge(zipModel, destDir.resolve(fileName + ".data"), block.getRecordBlock(signature));
        }
    }

    private ExtraFieldView createView() {
        return ExtraFieldView.builder()
                             .extraField(extraField)
                             .block(block)
                             .generalPurposeFlag(generalPurposeFlag)
                             .getDataFunc(Utils.getDataFunc(zipModel))
                             .position(0, settings.getColumnWidth()).build();
    }

}
