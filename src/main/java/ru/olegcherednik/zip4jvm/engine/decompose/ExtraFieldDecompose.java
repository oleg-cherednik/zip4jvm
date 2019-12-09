package ru.olegcherednik.zip4jvm.engine.decompose;

import ru.olegcherednik.zip4jvm.model.ExtraField;
import ru.olegcherednik.zip4jvm.model.GeneralPurposeFlag;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.block.ExtraFieldBlock;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;
import ru.olegcherednik.zip4jvm.view.extrafield.ExtraFieldRecordView;
import ru.olegcherednik.zip4jvm.view.extrafield.ExtraFieldView;

import java.io.IOException;
import java.io.PrintStream;
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

    public boolean printTextInfo(PrintStream out, boolean emptyLine) {
        return extraField != ExtraField.NULL && createView().print(out, emptyLine);
    }

    public void write(Path dir) throws IOException {
        if (extraField == ExtraField.NULL)
            return;

        dir = dir.resolve("extra_fields");
        Files.createDirectories(dir);

        ExtraFieldView view = createView();

        for (int signature : extraField.getSignatures()) {
            ExtraFieldRecordView<?> recordView = view.getView(extraField.getRecord(signature));
            String fileName = recordView.getFileName();

            Utils.print(dir.resolve(fileName + ".txt"), recordView::print);
            Utils.copyLarge(zipModel, dir.resolve(fileName + ".data"), block.getRecordBlock(signature));
        }
    }

    private ExtraFieldView createView() {
        return ExtraFieldView.builder()
                             .extraField(extraField)
                             .block(block)
                             .generalPurposeFlag(generalPurposeFlag)
                             .getDataFunc(Utils.getDataFunc(zipModel))
                             .position(settings.getOffs(), settings.getColumnWidth()).build();
    }

}
