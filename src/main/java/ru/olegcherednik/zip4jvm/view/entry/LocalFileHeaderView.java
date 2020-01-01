package ru.olegcherednik.zip4jvm.view.entry;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.olegcherednik.zip4jvm.model.Charsets;
import ru.olegcherednik.zip4jvm.model.CompressionMethod;
import ru.olegcherednik.zip4jvm.model.ExtraField;
import ru.olegcherednik.zip4jvm.model.GeneralPurposeFlag;
import ru.olegcherednik.zip4jvm.model.LocalFileHeader;
import ru.olegcherednik.zip4jvm.model.block.ZipEntryBlock;
import ru.olegcherednik.zip4jvm.view.BaseView;
import ru.olegcherednik.zip4jvm.view.CompressionMethodView;
import ru.olegcherednik.zip4jvm.view.GeneralPurposeFlagView;
import ru.olegcherednik.zip4jvm.view.LastModifiedTimeView;
import ru.olegcherednik.zip4jvm.view.SizeView;
import ru.olegcherednik.zip4jvm.view.StringHexView;
import ru.olegcherednik.zip4jvm.view.VersionView;
import ru.olegcherednik.zip4jvm.view.extrafield.ExtraFieldView;

import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.Optional;

/**
 * @author Oleg Cherednik
 * @since 23.10.2019
 */
public final class LocalFileHeaderView extends BaseView {

    private final LocalFileHeader localFileHeader;
    private final ZipEntryBlock.LocalFileHeaderBlock diagLocalFileHeader;
    private final long pos;
    private final Charset charset;

    public static Builder builder() {
        return new Builder();
    }

    private LocalFileHeaderView(Builder builder) {
        super(builder.offs, builder.columnWidth, builder.totalDisks);
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
        new VersionView(null, localFileHeader.getVersionToExtract(), offs, columnWidth).print(out);
    }

    private void printGeneralPurposeFlag(PrintStream out) {
        new GeneralPurposeFlagView(localFileHeader.getGeneralPurposeFlag(), localFileHeader.getCompressionMethod(), offs, columnWidth).print(out);
    }

    private void printCompressionMethod(PrintStream out) {
        CompressionMethod compressionMethod = localFileHeader.getCompressionMethod();
        GeneralPurposeFlag generalPurposeFlag = localFileHeader.getGeneralPurposeFlag();
        new CompressionMethodView(compressionMethod, generalPurposeFlag, offs, columnWidth).print(out);
    }

    private void printLastModifiedTime(PrintStream out) {
        new LastModifiedTimeView(localFileHeader.getLastModifiedTime(), offs, columnWidth).print(out);
    }

    private void printCrc(PrintStream out) {
        printLine(out, "32-bit CRC value:", String.format("0x%08X", localFileHeader.getCrc32()));
    }

    private void printSize(PrintStream out) {
        new SizeView("compressed size:", localFileHeader.getCompressedSize(), offs, columnWidth).print(out);
        new SizeView("uncompressed size:", localFileHeader.getUncompressedSize(), offs, columnWidth).print(out);
    }

    private void printFileName(PrintStream out) {
        printLine(out, "length of filename:", String.valueOf(localFileHeader.getFileName().length()));
        new StringHexView(localFileHeader.getFileName(), charset, offs, columnWidth).print(out);
    }

    private void printExtraField(PrintStream out) {
        if (localFileHeader.getExtraField() == ExtraField.NULL)
            return;

        ExtraFieldView.builder()
                      .extraField(localFileHeader.getExtraField())
                      .block(diagLocalFileHeader.getExtraFieldBlock())
                      .generalPurposeFlag(localFileHeader.getGeneralPurposeFlag())
                      .position(offs, columnWidth).build().printLocation(out);
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Builder {

        private LocalFileHeader localFileHeader;
        private ZipEntryBlock.LocalFileHeaderBlock diagLocalFileHeader;
        private long totalDisks;
        private long pos;
        private Charset charset = Charsets.IBM437;
        private int offs;
        private int columnWidth;

        public LocalFileHeaderView build() {
            return new LocalFileHeaderView(this);
        }

        public Builder localFileHeader(LocalFileHeader localFileHeader) {
            this.localFileHeader = localFileHeader;
            return this;
        }

        public Builder diagLocalFileHeader(ZipEntryBlock.LocalFileHeaderBlock diagLocalFileHeader) {
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

        public Builder position(int offs, int columnWidth, long totalDisks) {
            this.offs = offs;
            this.columnWidth = columnWidth;
            this.totalDisks = totalDisks;
            return this;
        }
    }

}
