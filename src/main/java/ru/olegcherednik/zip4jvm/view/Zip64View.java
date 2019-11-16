package ru.olegcherednik.zip4jvm.view;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.block.Zip64Block;

import java.io.PrintStream;
import java.util.Objects;

/**
 * @author Oleg Cherednik
 * @since 14.10.2019
 */
public final class Zip64View extends View {

    private final Zip64 zip64;
    private final Zip64Block block;

    public static Builder builder() {
        return new Builder();
    }

    private Zip64View(Builder builder) {
        super(builder.offs, builder.columnWidth);
        zip64 = builder.zip64;
        block = builder.block;
    }

    @Override
    public boolean print(PrintStream out) {
        boolean emptyLine = createEndCentralDirectorLocatorView().print(out);
        return createEndCentralDirectoryView().print(out, emptyLine);
    }

    private EndCentralDirectoryLocatorView createEndCentralDirectorLocatorView() {
        return EndCentralDirectoryLocatorView.builder()
                                             .locator(zip64.getEndCentralDirectoryLocator())
                                             .block(block.getEndCentralDirectoryLocatorBlock())
                                             .offs(offs)
                                             .columnWidth(columnWidth).build();
    }

    private EndCentralDirectoryView createEndCentralDirectoryView() {
        return EndCentralDirectoryView.builder()
                                      .endCentralDirectory(zip64.getEndCentralDirectory())
                                      .block(block.getEndCentralDirectoryBlock())
                                      .offs(offs)
                                      .columnWidth(columnWidth).build();
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Builder {

        private Zip64 zip64;
        private Zip64Block block;
        private int offs;
        private int columnWidth;

        public Zip64View build() {
            Objects.requireNonNull(zip64, "'zip64' must not be null");
            Objects.requireNonNull(block, "'block' must not be null");
            return new Zip64View(this);
        }

        public Builder zip64(Zip64 zip64) {
            this.zip64 = zip64 == Zip64.NULL ? null : zip64;
            return this;
        }

        public Builder block(Zip64Block block) {
            this.block = block;
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

    public static final class EndCentralDirectoryLocatorView extends View {

        private final Zip64.EndCentralDirectoryLocator locator;
        private final Block block;

        public static EndCentralDirectoryLocatorView.Builder builder() {
            return new EndCentralDirectoryLocatorView.Builder();
        }

        private EndCentralDirectoryLocatorView(EndCentralDirectoryLocatorView.Builder builder) {
            super(builder.offs, builder.columnWidth);
            locator = builder.locator;
            block = builder.block;
        }

        @Override
        public boolean print(PrintStream out) {
            printTitle(out, Zip64.EndCentralDirectoryLocator.SIGNATURE, "ZIP64 End of Central directory locator", block);
            printLine(out, String.format("part number of new-end-of-central-dir (%04X):", locator.getMainDisk()), locator.getMainDisk() + 1);
            printLine(out, "relative offset of new-end-of-central-dir:", String.format("%1$d (0x%1$08X) bytes", locator.getOffs()));
            printLine(out, "total number of parts in archive:", locator.getTotalDisks());
            return true;
        }

        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static final class Builder {

            private Zip64.EndCentralDirectoryLocator locator;
            private Block block;
            private int offs;
            private int columnWidth;

            public EndCentralDirectoryLocatorView build() {
                Objects.requireNonNull(locator, "'locator' must not be null");
                Objects.requireNonNull(block, "'block' must not be null");
                return new EndCentralDirectoryLocatorView(this);
            }

            public EndCentralDirectoryLocatorView.Builder locator(Zip64.EndCentralDirectoryLocator locator) {
                this.locator = locator;
                return this;
            }

            public EndCentralDirectoryLocatorView.Builder block(Block block) {
                this.block = block == Block.NULL ? null : block;
                return this;
            }

            public EndCentralDirectoryLocatorView.Builder offs(int offs) {
                this.offs = offs;
                return this;
            }

            public EndCentralDirectoryLocatorView.Builder columnWidth(int columnWidth) {
                this.columnWidth = columnWidth;
                return this;
            }
        }
    }

    public static final class EndCentralDirectoryView extends View {

        private final Zip64.EndCentralDirectory dir;
        private final Block block;

        public static EndCentralDirectoryView.Builder builder() {
            return new EndCentralDirectoryView.Builder();
        }

        private EndCentralDirectoryView(EndCentralDirectoryView.Builder builder) {
            super(builder.offs, builder.columnWidth);
            dir = builder.endCentralDirectory;
            block = builder.block;
        }

        @Override
        public boolean print(PrintStream out) {
            printTitle(out, Zip64.EndCentralDirectory.SIGNATURE, "ZIP64 End of Central directory record", block);
            printLine(out, "number of bytes in rest of record:", String.format("%d bytes", dir.getEndCentralDirectorySize()));
            printVersion(out);
            printLine(out, String.format("part number of this part (%04d):", dir.getTotalDisks()), dir.getTotalDisks() + 1);
            printLine(out, String.format("part number of start of central dir (%04d):", dir.getMainDisk()), dir.getMainDisk() + 1);
            printLine(out, "number of entries in central dir in this part:", dir.getDiskEntries());
            printLine(out, "total number of entries in central dir:", dir.getTotalEntries());
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
            private Block block;
            private int offs;
            private int columnWidth;

            public EndCentralDirectoryView build() {
                Objects.requireNonNull(endCentralDirectory, "'endCentralDirectory' must not be null");
                Objects.requireNonNull(block, "'block' must not be null");
                return new EndCentralDirectoryView(this);
            }

            public EndCentralDirectoryView.Builder endCentralDirectory(Zip64.EndCentralDirectory endCentralDirectory) {
                this.endCentralDirectory = endCentralDirectory;
                return this;
            }

            public EndCentralDirectoryView.Builder block(Block block) {
                this.block = block == Block.NULL ? null : block;
                return this;
            }

            public EndCentralDirectoryView.Builder offs(int offs) {
                this.offs = offs;
                return this;
            }

            public EndCentralDirectoryView.Builder columnWidth(int columnWidth) {
                this.columnWidth = columnWidth;
                return this;
            }
        }
    }

}
