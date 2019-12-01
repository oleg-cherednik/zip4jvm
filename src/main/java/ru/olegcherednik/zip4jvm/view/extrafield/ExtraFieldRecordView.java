package ru.olegcherednik.zip4jvm.view.extrafield;

import org.apache.commons.lang.ArrayUtils;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.view.View;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Objects;

/**
 * @author Oleg Cherednik
 * @since 24.11.2019
 */
public abstract class ExtraFieldRecordView extends View {

    protected final Block block;
    protected final Path file;

    protected ExtraFieldRecordView(Block block, Path file, int offs, int columnWidth) {
        super(offs, columnWidth);
        this.block = block;
        this.file = file;
    }

    protected abstract int getSignature();

    protected abstract String getTitle();

    protected final byte[] getData() {
        return ArrayUtils.EMPTY_BYTE_ARRAY;
    }

    public String getFileName() {
        String title = getTitle();
        title = title.replaceAll(" ", "_");
        title = title.replaceAll("[/\\\\]", "-");
        return String.format("(0x%04X)_%s", getSignature(), title);
    }

    @Override
    public boolean print(PrintStream out) {
        printValueLocation(out, String.format("(0x%04X) %s:", getSignature(), getTitle()), block);
        return true;
    }

    protected abstract static class BaseBuilder<T extends BaseBuilder<?>> {

        protected Block block;
        protected Path file;
        protected int offs;
        protected int columnWidth;

        protected void check() {
            Objects.requireNonNull(block, "'block' must not be null");
//            Objects.requireNonNull(file, "'file' must not be null");
        }

        public final T block(Block block) {
            this.block = block == Block.NULL ? null : block;
            return (T)this;
        }

        public final T file(Path file) {
            this.file = file;
            return (T)this;
        }

        public final T offs(int offs) {
            this.offs = offs;
            return (T)this;
        }

        public final T columnWidth(int columnWidth) {
            this.columnWidth = columnWidth;
            return (T)this;
        }
    }
}
