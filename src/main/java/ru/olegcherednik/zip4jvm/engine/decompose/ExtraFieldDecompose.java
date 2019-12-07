package ru.olegcherednik.zip4jvm.engine.decompose;

import ru.olegcherednik.zip4jvm.model.ExtraField;
import ru.olegcherednik.zip4jvm.model.GeneralPurposeFlag;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.block.ExtraFieldBlock;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;
import ru.olegcherednik.zip4jvm.view.extrafield.ExtraFieldRecordView;
import ru.olegcherednik.zip4jvm.view.extrafield.ExtraFieldView;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 07.12.2019
 */
public class ExtraFieldDecompose extends BaseDecompose {

    private final ExtraField extraField;
    private final ExtraFieldBlock block;
    private final GeneralPurposeFlag generalPurposeFlag;

    public ExtraFieldDecompose(ZipModel zipModel, ZipInfoSettings settings, ExtraField extraField, ExtraFieldBlock block,
            GeneralPurposeFlag generalPurposeFlag) {
        super(zipModel, settings);
        this.extraField = extraField;
        this.block = block;
        this.generalPurposeFlag = generalPurposeFlag;
    }

    @Override
    protected ExtraFieldView createView() {
        return ExtraFieldView.builder()
                             .extraField(extraField)
                             .block(block)
                             .generalPurposeFlag(generalPurposeFlag)
                             .getDataFunc(getDataFunc(zipModel))
                             .position(0, settings.getColumnWidth()).build();
    }

    @Override
    public void write(Path destDir) throws IOException {
        destDir = destDir.resolve("extra_fields");
        Files.createDirectories(destDir);

        ExtraFieldView view = createView();

        for (int signature : extraField.getSignatures()) {
            ExtraFieldRecordView<?> recordView = view.getView(extraField.getRecord(signature));

            // print .txt
            try (PrintStream out = new PrintStream(new FileOutputStream(destDir.resolve(recordView.getFileName() + ".txt").toFile()))) {
                recordView.print(out);
            }

            // print .data
            copyLarge(zipModel.getFile(), destDir.resolve(recordView.getFileName() + ".data"), block.getRecordBlock(signature));
        }
    }

}
