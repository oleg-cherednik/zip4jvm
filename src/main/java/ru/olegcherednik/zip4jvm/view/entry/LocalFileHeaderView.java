package ru.olegcherednik.zip4jvm.view.entry;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.olegcherednik.zip4jvm.model.Charsets;
import ru.olegcherednik.zip4jvm.model.LocalFileHeader;
import ru.olegcherednik.zip4jvm.model.block.Diagnostic;
import ru.olegcherednik.zip4jvm.view.CompressionMethodView;
import ru.olegcherednik.zip4jvm.view.GeneralPurposeFlagView;
import ru.olegcherednik.zip4jvm.view.IView;
import ru.olegcherednik.zip4jvm.view.LastModifiedTimeView;
import ru.olegcherednik.zip4jvm.view.StringHexView;
import ru.olegcherednik.zip4jvm.view.VersionView;
import ru.olegcherednik.zip4jvm.view.View;
import ru.olegcherednik.zip4jvm.view.extrafield.ExtraFieldView;

import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.Optional;

/**
 * @author Oleg Cherednik
 * @since 23.10.2019
 */
final class LocalFileHeaderView extends View {

    private final LocalFileHeader localFileHeader;
    // TODO should be block
    private final Diagnostic.ZipEntryBlock.LocalFileHeaderB diagLocalFileHeader;
    private final long pos;
    private final Charset charset;

    public static Builder builder() {
        return new Builder();
    }

    private LocalFileHeaderView(Builder builder) {
        super(builder.offs, builder.columnWidth);
        localFileHeader = builder.localFileHeader;
        diagLocalFileHeader = builder.diagLocalFileHeader;
        pos = builder.pos;
        charset = builder.charset;
    }

    @Override
    public boolean print(PrintStream out) {
        printSubTitle(out, LocalFileHeader.SIGNATURE, pos, '[' + charset.name() + "] " + localFileHeader.getFileName(),
                diagLocalFileHeader.getContent());
        printVersion(out);
        printGeneralPurposeFlag(out);
        printCompressionMethod(out);
        printLastModifiedTime(out);
        printCrc(out);
        printSize(out);
        printFileName(out);
        printExtraField(out);

        return true;
    }

    private void printVersion(PrintStream out) {
        VersionView.builder()
                   .versionToExtract(localFileHeader.getVersionToExtract())
                   .offs(offs)
                   .columnWidth(columnWidth).build().print(out);
    }

    private void printGeneralPurposeFlag(PrintStream out) {
        GeneralPurposeFlagView.builder()
                              .generalPurposeFlag(localFileHeader.getGeneralPurposeFlag())
                              .compressionMethod(localFileHeader.getCompressionMethod())
                              .offs(offs)
                              .columnWidth(columnWidth).build().print(out);
    }

    private void printCompressionMethod(PrintStream out) {
        CompressionMethodView.builder()
                             .compressionMethod(localFileHeader.getCompressionMethod())
                             .generalPurposeFlag(localFileHeader.getGeneralPurposeFlag())
                             .offs(offs)
                             .columnWidth(columnWidth).build().print(out);
    }

    private void printLastModifiedTime(PrintStream out) {
        LastModifiedTimeView.builder()
                            .lastModifiedTime(localFileHeader.getLastModifiedTime())
                            .offs(offs)
                            .columnWidth(columnWidth).build().print(out);
    }

    private void printCrc(PrintStream out) {
        printLine(out, "32-bit CRC value:", String.format("0x%08X", localFileHeader.getCrc32()));
    }

    private void printSize(PrintStream out) {
        printLine(out, "compressed size:", String.valueOf(localFileHeader.getCompressedSize()));
        printLine(out, "uncompressed size:", String.valueOf(localFileHeader.getUncompressedSize()));
    }

    private void printFileName(PrintStream out) {
        printLine(out, "length of filename:", String.valueOf(localFileHeader.getFileName().length()));

        StringHexView.builder()
                     .str(localFileHeader.getFileName())
                     .charset(charset)
                     .offs(offs)
                     .columnWidth(columnWidth).build().print(out);
    }

    private void printExtraField(PrintStream out) {
        ExtraFieldView.builder()
                      .extraField(localFileHeader.getExtraField())
                      .diagExtraField(diagLocalFileHeader.getExtraField())
                      .generalPurposeFlag(localFileHeader.getGeneralPurposeFlag())
                      .offs(offs)
                      .columnWidth(columnWidth).build().print(out);
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Builder {

        private LocalFileHeader localFileHeader;
        private Diagnostic.ZipEntryBlock.LocalFileHeaderB diagLocalFileHeader;
        private long pos;
        private Charset charset = Charsets.IBM437;
        private int offs;
        private int columnWidth;

        public IView build() {
            return new LocalFileHeaderView(this);
        }

        public Builder localFileHeader(LocalFileHeader localFileHeader) {
            this.localFileHeader = localFileHeader;
            return this;
        }

        public Builder diagLocalFileHeader(Diagnostic.ZipEntryBlock.LocalFileHeaderB diagLocalFileHeader) {
            this.diagLocalFileHeader = diagLocalFileHeader;
            return this;
        }

        public Builder pos(long pos) {
            this.pos = pos;
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
