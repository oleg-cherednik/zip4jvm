package ru.olegcherednik.zip4jvm.engine.decompose;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.io.in.SingleZipInputStream;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;
import ru.olegcherednik.zip4jvm.view.IView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 06.12.2019
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseDecompose {

    protected final ZipModel zipModel;
    protected final ZipInfoSettings settings;

    public final boolean print(PrintStream out, boolean emptyLine) {
        return createView().print(out, emptyLine);
    }

    protected static void print(Path file, Consumer<PrintStream> consumer) throws FileNotFoundException {
        try (PrintStream out = new PrintStream(file.toFile())) {
            consumer.accept(out);
        }
    }

    public abstract void write(Path destDir) throws IOException;

    protected abstract IView createView();

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

    protected static Function<Block, byte[]> getDataFunc(ZipModel zipModel) {
        return block -> {
            if (block.getSize() > Integer.MAX_VALUE)
                return ArrayUtils.EMPTY_BYTE_ARRAY;

            try (DataInput in = new SingleZipInputStream(zipModel.getFile())) {
                in.skip(block.getOffs());
                return in.readBytes((int)block.getSize());
            } catch(Exception e) {
                e.printStackTrace();
                return ArrayUtils.EMPTY_BYTE_ARRAY;
            }
        };
    }

}
