package ru.olegcherednik.zip4jvm.view.centraldirectory;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.Charsets;
import ru.olegcherednik.zip4jvm.model.block.CentralDirectoryBlock;
import ru.olegcherednik.zip4jvm.view.View;

import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Oleg Cherednik
 * @since 14.10.2019
 */
public final class CentralDirectoryView extends View {

    private final CentralDirectory centralDirectory;
    private final CentralDirectoryBlock diagCentralDirectory;
    private final Charset charset;

    public static Builder builder() {
        return new Builder();
    }

    private CentralDirectoryView(Builder builder) {
        super(builder.offs, builder.columnWidth);
        centralDirectory = builder.centralDirectory;
        diagCentralDirectory = builder.diagCentralDirectory;
        charset = builder.charset;
    }

    @Override
    public boolean print(PrintStream out) {
        printTitle(out, CentralDirectory.FileHeader.SIGNATURE, "Central directory", diagCentralDirectory);
        printLine(out, "total entries:", String.valueOf(centralDirectory.getFileHeaders().size()));

        boolean emptyLine = createFileHeaderListView().print(out);
        return createDigitalSignatureView().print(out, emptyLine);
    }

    public boolean printHeader(PrintStream out, boolean emptyLine) {
        if (emptyLine)
            out.println();

        printTitle(out, CentralDirectory.FileHeader.SIGNATURE, "Central directory", diagCentralDirectory);
        printLine(out, "total entries:", String.valueOf(centralDirectory.getFileHeaders().size()));
        return true;
    }

    private FileHeaderListView createFileHeaderListView() {
        return FileHeaderListView.builder()
                                 .centralDirectory(centralDirectory)
                                 .diagCentralDirectory(diagCentralDirectory)
                                 .charset(charset)
                                 .offs(offs)
                                 .columnWidth(columnWidth).build();
    }

    private DigitalSignatureView createDigitalSignatureView() {
        return DigitalSignatureView.builder()
                                   .digitalSignature(centralDirectory.getDigitalSignature())
                                   .block(diagCentralDirectory.getDigitalSignatureBlock())
                                   .offs(offs)
                                   .columnWidth(columnWidth).build();
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Builder {

        private CentralDirectory centralDirectory;
        private CentralDirectoryBlock diagCentralDirectory;
        private Charset charset = Charsets.IBM437;
        private int offs;
        private int columnWidth;

        public CentralDirectoryView build() {
            Objects.requireNonNull(centralDirectory, "'centralDirectory' must not be null");
            Objects.requireNonNull(diagCentralDirectory, "'diagCentralDirectory' must not be null");
            return new CentralDirectoryView(this);
        }

        public Builder centralDirectory(CentralDirectory centralDirectory) {
            this.centralDirectory = centralDirectory;
            return this;
        }

        public Builder diagCentralDirectory(CentralDirectoryBlock diagCentralDirectory) {
            this.diagCentralDirectory = diagCentralDirectory;
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
