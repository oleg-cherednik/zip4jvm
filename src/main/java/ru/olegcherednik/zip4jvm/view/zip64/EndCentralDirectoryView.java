package ru.olegcherednik.zip4jvm.view.zip64;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.view.VersionView;
import ru.olegcherednik.zip4jvm.view.View;

import java.io.PrintStream;
import java.util.Optional;

/**
 * @author Oleg Cherednik
 * @since 11.11.2019
 */
final class EndCentralDirectoryView extends View {

    private final Zip64.EndCentralDirectory dir;
    private final Block block;

    public static Builder builder() {
        return new Builder();
    }

    private EndCentralDirectoryView(Builder builder) {
        super(builder.offs, builder.columnWidth);
        dir = builder.endCentralDirectory;
        block = builder.block;
    }

    @Override
    public boolean print(PrintStream out) {
        if (dir == null || block == Block.NULL)
            return false;

        printTitle(out, Zip64.EndCentralDirectory.SIGNATURE, "ZIP64 End of Central directory record", block);
        printLine(out, "number of bytes in rest of record:", String.format("%d bytes", dir.getEndCentralDirectorySize()));
        printVersion(out);
        printLine(out, String.format("part number of this part (%04d):", dir.getTotalDisks()), String.valueOf(dir.getTotalDisks() + 1));
        printLine(out, String.format("part number of start of central dir (%04d):", dir.getMainDisk()), String.valueOf(dir.getMainDisk() + 1));
        printLine(out, "number of entries in central dir in this part:", String.valueOf(dir.getDiskEntries()));
        printLine(out, "total number of entries in central dir:", String.valueOf(dir.getTotalEntries()));
        printLine(out, "size of central dir:", String.format("%1$d (0x%1$08X) bytes", dir.getCentralDirectorySize()));
        printLine(out, "relative offset of central dir:", String.format("%1$d (0x%1$08X) bytes", dir.getCentralDirectoryOffs()));

        return true;
    }

    private void printVersion(PrintStream out) {
        VersionView.builder()
                   .versionMadeBy(dir.getVersionMadeBy())
                   .versionToExtract(dir.getVersionToExtract())
                   .offs(offs)
                   .columnWidth(columnWidth).build().print(out);
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Builder {

        private Zip64.EndCentralDirectory endCentralDirectory;
        private Block block = Block.NULL;
        private int offs;
        private int columnWidth;

        public EndCentralDirectoryView build() {
            return new EndCentralDirectoryView(this);
        }

        public Builder endCentralDirectory(Zip64.EndCentralDirectory endCentralDirectory) {
            this.endCentralDirectory = endCentralDirectory;
            return this;
        }

        public Builder block(Block block) {
            this.block = Optional.ofNullable(block).orElse(Block.NULL);
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
