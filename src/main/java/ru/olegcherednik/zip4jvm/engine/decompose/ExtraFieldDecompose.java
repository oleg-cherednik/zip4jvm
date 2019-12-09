package ru.olegcherednik.zip4jvm.engine.decompose;

import ru.olegcherednik.zip4jvm.model.ExtraField;
import ru.olegcherednik.zip4jvm.model.GeneralPurposeFlag;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.block.ExtraFieldBlock;
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
    private final ExtraField extraField;
    private final ExtraFieldBlock block;
    private final GeneralPurposeFlag generalPurposeFlag;
    private final int offs;
    private final int columnWidth;

    public ExtraFieldDecompose(ZipModel zipModel, ExtraField extraField, ExtraFieldBlock block, GeneralPurposeFlag generalPurposeFlag, int offs,
            int columnWidth) {
        this.zipModel = zipModel;
        this.extraField = extraField;
        this.block = block;
        this.generalPurposeFlag = generalPurposeFlag;
        this.offs = offs;
        this.columnWidth = columnWidth;
    }

    public boolean printTextInfo(PrintStream out, boolean emptyLine) {
        return extraField != ExtraField.NULL && createView().print(out, emptyLine);
    }

    public void decompose(Path dir) throws IOException {
        if (extraField == ExtraField.NULL)
            return;

        dir = dir.resolve("extra_fields");
        Files.createDirectories(dir);

        ExtraFieldView view = createView();

        for (int signature : extraField.getSignatures()) {
            ExtraFieldRecordView<?> recordView = view.getView(extraField.getRecord(signature));
            String fileName = recordView.getFileName();

            DecomposeUtils.print(dir.resolve(fileName + ".txt"), recordView::print);
            DecomposeUtils.copyLarge(zipModel, dir.resolve(fileName + ".data"), block.getRecordBlock(signature));
        }
    }

    private ExtraFieldView createView() {
        return ExtraFieldView.builder()
                             .extraField(extraField)
                             .block(block)
                             .generalPurposeFlag(generalPurposeFlag)
                             .getDataFunc(DecomposeUtils.getDataFunc(zipModel))
                             .position(offs, columnWidth).build();
    }

}
