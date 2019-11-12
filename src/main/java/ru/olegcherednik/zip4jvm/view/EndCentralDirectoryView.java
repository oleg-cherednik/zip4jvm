package ru.olegcherednik.zip4jvm.view;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.olegcherednik.zip4jvm.model.Charsets;
import ru.olegcherednik.zip4jvm.model.EndCentralDirectory;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.block.Block;

import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.Optional;

/**
 * @author Oleg Cherednik
 * @since 13.10.2019
 */
public final class EndCentralDirectoryView extends View {

    private final EndCentralDirectory dir;
    private final Block block;
    private final Charset charset;

    public static Builder builder() {
        return new Builder();
    }

    private EndCentralDirectoryView(Builder builder) {
        super(builder.offs, builder.columnWidth);
        dir = builder.endCentralDirectory;
        block = builder.block;
        charset = builder.charset;
    }

    @Override
    public boolean print(PrintStream out) {
        printTitle(out, EndCentralDirectory.SIGNATURE, "End of Central directory record", block);
        printLine(out, String.format("part number of this part (%04X):", dir.getTotalDisks()), dir.getTotalDisks() + 1);
        printLine(out, String.format("part number of start of central dir (%04X):", dir.getMainDisk()), dir.getMainDisk() + 1);
        printLine(out, "number of entries in central dir in this part:", dir.getDiskEntries());
        printLine(out, "total number of entries in central dir:", dir.getTotalEntries());
        printLine(out, "size of central dir:", String.format("%1$d (0x%1$08X) bytes", dir.getCentralDirectorySize()));
        printCentralDirectoryOffs(out);
        printComment(out);
        return true;
    }

    private void printCentralDirectoryOffs(PrintStream out) {
        printLine(out, "relative offset of central dir:", String.format("%1$d (0x%1$08X) bytes", dir.getCentralDirectoryOffs()));

        if (dir.getCentralDirectoryOffs() == Zip64.LIMIT_DWORD)
            printLine(out, "  (see real value in ZIP64 record)");
    }

    private void printComment(PrintStream out) {
        String comment = Optional.ofNullable(dir.getComment()).orElse("");
        printLine(out, "zipfile comment length:", String.format("%d bytes", comment.getBytes(charset).length));

        if (comment.isEmpty())
            return;

        StringHexView.builder()
                     .str(comment)
                     .charset(charset)
                     .offs(offs)
                     .columnWidth(columnWidth).build().print(out);
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Builder {

        private EndCentralDirectory endCentralDirectory;
        private Block block = Block.NULL;
        private Charset charset = Charsets.IBM437;
        private int offs;
        private int columnWidth;

        public IView build() {
            return endCentralDirectory == null || block == Block.NULL ? IView.NULL : new EndCentralDirectoryView(this);
        }

        public Builder endCentralDirectory(EndCentralDirectory endCentralDirectory) {
            this.endCentralDirectory = endCentralDirectory;
            return this;
        }

        public Builder block(Block block) {
            this.block = Optional.ofNullable(block).orElse(Block.NULL);
            return this;
        }

        public Builder charset(Charset charset) {
            this.charset = Optional.ofNullable(charset).orElse(Charsets.IBM437);
            return this;
        }

        public Builder offs(int offs) {
            this.offs = offs;
            return this;
        }

        public Builder columnWidth(int columnWidth) {
            this.columnWidth = columnWidth;
            return this;
        }
    }
}

