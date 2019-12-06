package ru.olegcherednik.zip4jvm.engine.decompose;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.io.in.SingleZipInputStream;
import ru.olegcherednik.zip4jvm.model.ExtraField;
import ru.olegcherednik.zip4jvm.model.GeneralPurposeFlag;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.block.BlockModel;
import ru.olegcherednik.zip4jvm.model.block.ExtraFieldBlock;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;
import ru.olegcherednik.zip4jvm.view.IView;
import ru.olegcherednik.zip4jvm.view.extrafield.ExtraFieldRecordView;
import ru.olegcherednik.zip4jvm.view.extrafield.ExtraFieldView;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 06.12.2019
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
abstract class BaseDecompose {

    protected final BlockModel blockModel;
    protected final ZipInfoSettings settings;

    public abstract IView createView();

    public abstract void write(Path destDir) throws IOException;

    protected static void copyLarge(Path in, Path out, Block block) throws IOException {
        copyLarge(in, out, block.getOffs(), block.getSize());
    }

    protected static void copyLarge(Path in, Path out, long offs, long size) throws IOException {
        try (FileInputStream fis = new FileInputStream(in.toFile()); FileOutputStream fos = new FileOutputStream(out.toFile())) {
            copyLarge(fis, fos, offs, size);
        }
    }

    protected static void copyLarge(FileInputStream in, FileOutputStream out, long offs, long size) throws IOException {
        in.skip(offs);
        IOUtils.copyLarge(in, out, 0, size);
    }

    protected static Function<Block, byte[]> getDataFunc(BlockModel blockModel) {
        return block -> {
            if (block.getSize() > Integer.MAX_VALUE)
                return ArrayUtils.EMPTY_BYTE_ARRAY;

            try (DataInput in = new SingleZipInputStream(blockModel.getZipModel().getFile())) {
                in.skip(block.getOffs());
                return in.readBytes((int)block.getSize());
            } catch(Exception e) {
                e.printStackTrace();
                return ArrayUtils.EMPTY_BYTE_ARRAY;
            }
        };
    }

    protected void writeExtraField(ExtraField extraField, ExtraFieldBlock block, GeneralPurposeFlag generalPurposeFlag, Path parent)
            throws IOException {
        if (extraField == ExtraField.NULL)
            return;

        Path dir = parent.resolve("extra_fields");
        Files.createDirectories(dir);

        ExtraFieldView extraFieldView = ExtraFieldView.builder()
                                                      .extraField(extraField)
                                                      .block(block)
                                                      .generalPurposeFlag(generalPurposeFlag)
                                                      .getDataFunc(getDataFunc(blockModel))
                                                      .position(0, settings.getColumnWidth()).build();

        for (int signature : extraField.getSignatures()) {
            ExtraField.Record record = extraField.getRecord(signature);
            ExtraFieldRecordView<?> recordView = extraFieldView.getView(record);

            // print .txt
            try (PrintStream out = new PrintStream(new FileOutputStream(dir.resolve(recordView.getFileName() + ".txt").toFile()))) {
                recordView.print(out);
            }

            // print .data
            copyLarge(blockModel.getZipModel().getFile(), dir.resolve(recordView.getFileName() + ".data"), block.getRecordBlock(signature));
        }
    }

}
