package ru.olegcherednik.zip4jvm.view.centraldirectory;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.Charsets;
import ru.olegcherednik.zip4jvm.model.block.CentralDirectoryBlock;
import ru.olegcherednik.zip4jvm.view.View;

import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.Optional;

/**
 * @author Oleg Cherednik
 * @since 11.11.2019
 */
final class FileHeaderListView extends View {

    private final CentralDirectory centralDirectory;
    private final CentralDirectoryBlock diagCentralDirectory;
    private final Charset charset;

    public static Builder builder() {
        return new Builder();
    }

    private FileHeaderListView(Builder builder) {
        super(builder.offs, builder.columnWidth);
        centralDirectory = builder.centralDirectory;
        diagCentralDirectory = builder.diagCentralDirectory;
        charset = builder.charset;
    }

    @Override
    public boolean print(PrintStream out) {
        if (centralDirectory.getFileHeaders().isEmpty())
            return false;

        out.println();

        int pos = 0;

        for (CentralDirectory.FileHeader fileHeader : centralDirectory.getFileHeaders()) {
            if (pos != 0)
                out.println();

            FileHeaderView.builder()
                          .fileHeader(fileHeader)
                          .diagFileHeader(diagCentralDirectory.getFileHeader(fileHeader.getFileName()))
                          .pos(pos++)
                          .charset(charset)
                          .offs(offs)
                          .columnWidth(columnWidth).build().print(out);
        }

        return true;
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Builder {

        private CentralDirectory centralDirectory;
        private CentralDirectoryBlock diagCentralDirectory;
        private Charset charset = Charsets.IBM437;
        private int offs;
        private int columnWidth;

        public FileHeaderListView build() {
            return new FileHeaderListView(this);
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
