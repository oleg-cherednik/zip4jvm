package ru.olegcherednik.zip4jvm.view.zip64;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.view.View;

import java.io.PrintStream;
import java.util.Optional;

/**
 * @author Oleg Cherednik
 * @since 10.11.2019
 */
final class EndCentralDirectoryLocatorView extends View {

    private final Zip64.EndCentralDirectoryLocator locator;
    private final Block block;

    public static Builder builder() {
        return new Builder();
    }

    private EndCentralDirectoryLocatorView(Builder builder) {
        super(builder.offs, builder.columnWidth);
        locator = builder.locator;
        block = builder.block;
    }

    @Override
    public boolean print(PrintStream out) {
        if (locator == null || block == Block.NULL)
            return false;

        printTitle(out, Zip64.EndCentralDirectoryLocator.SIGNATURE, "ZIP64 End of Central directory locator", block);
        printLine(out, String.format("part number of new-end-of-central-dir (%04X):", locator.getMainDisk()),
                String.valueOf(locator.getMainDisk() + 1));
        printLine(out, "relative offset of new-end-of-central-dir:", String.format("%1$d (0x%1$08X) bytes", locator.getOffs()));
        printLine(out, "total number of parts in archive:", String.valueOf(locator.getTotalDisks()));

        return true;
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Builder {

        private Zip64.EndCentralDirectoryLocator locator;
        private Block block = Block.NULL;
        private int offs;
        private int columnWidth;

        public EndCentralDirectoryLocatorView build() {
            return new EndCentralDirectoryLocatorView(this);
        }

        public Builder locator(Zip64.EndCentralDirectoryLocator locator) {
            this.locator = locator;
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
