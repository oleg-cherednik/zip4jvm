package ru.olegcherednik.zip4jvm.view.zip64;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.block.Diagnostic;
import ru.olegcherednik.zip4jvm.view.View;

import java.io.PrintStream;
import java.util.Optional;

/**
 * @author Oleg Cherednik
 * @since 14.10.2019
 */
public final class Zip64View extends View {

    private final Zip64 zip64;
    private final Diagnostic.Zip64 diagZip64;

    public static Builder builder() {
        return new Builder();
    }

    private Zip64View(Builder builder) {
        super(builder.offs, builder.columnWidth);
        zip64 = builder.zip64;
        diagZip64 = builder.diagZip64;
    }

    @Override
    public void print(PrintStream out) {
        printEndCentralDirectorLocator(out);
        out.println();
        printEndCentralDirectory(out);
    }

    private void printEndCentralDirectorLocator(PrintStream out) {
        EndCentralDirectoryLocatorView.builder()
                                      .locator(zip64.getEndCentralDirectoryLocator())
                                      .block(diagZip64.getEndCentralDirectoryLocator())
                                      .offs(offs)
                                      .columnWidth(columnWidth).build().print(out);
    }

    private void printEndCentralDirectory(PrintStream out) {
        EndCentralDirectoryView.builder()
                               .endCentralDirectory(zip64.getEndCentralDirectory())
                               .block(diagZip64.getEndCentralDirectory())
                               .offs(offs)
                               .columnWidth(columnWidth).build().print(out);
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Builder {

        private Zip64 zip64 = Zip64.NULL;
        private Diagnostic.Zip64 diagZip64 = Diagnostic.Zip64.NULL;
        private int offs;
        private int columnWidth;

        public Zip64View build() {
            return new Zip64View(this);
        }

        public Builder zip64(Zip64 zip64) {
            this.zip64 = Optional.ofNullable(zip64).orElse(Zip64.NULL);
            return this;
        }

        public Builder diagZip64(Diagnostic.Zip64 diagZip64) {
            this.diagZip64 = Optional.ofNullable(diagZip64).orElse(Diagnostic.Zip64.NULL);
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
