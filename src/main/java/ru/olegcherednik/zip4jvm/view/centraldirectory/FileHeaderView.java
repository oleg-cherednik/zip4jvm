package ru.olegcherednik.zip4jvm.view.centraldirectory;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.Charsets;
import ru.olegcherednik.zip4jvm.model.block.Diagnostic;
import ru.olegcherednik.zip4jvm.view.CompressionMethodView;
import ru.olegcherednik.zip4jvm.view.ExternalFileAttributesView;
import ru.olegcherednik.zip4jvm.view.GeneralPurposeFlagView;
import ru.olegcherednik.zip4jvm.view.InternalFileAttributesView;
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
 * @since 14.10.2019
 */
final class FileHeaderView extends View {

    private final CentralDirectory.FileHeader fileHeader;
    private final Diagnostic.CentralDirectory.FileHeader diagFileHeader;
    private final long pos;
    private final Charset charset;

    public static Builder builder() {
        return new Builder();
    }

    private FileHeaderView(Builder builder) {
        super(builder.offs, builder.columnWidth);
        fileHeader = builder.fileHeader;
        diagFileHeader = builder.diagFileHeader;
        pos = builder.pos;
        charset = builder.charset;
    }

    @Override
    public boolean print(PrintStream out) {
        printSubTitle(out, pos, charset, fileHeader.getFileName(), diagFileHeader);
        printLocation(out);
        printVersion(out);
        printGeneralPurposeFlag(out);
        printCompressionMethod(out);
        printLastModifiedTime(out);
        printCrc(out);
        printSize(out);
        printFileName(out);
        printComment(out);
        printInternalFileAttributesView(out);
        printExternalFileAttributes(out);
        printExtraField(out);
        return true;
    }

    private void printLocation(PrintStream out) {
        printLine(out, "location of central-directory-record:", String.format("%1$d (0x%1$08X) bytes", diagFileHeader.getOffs()));
        printLine(out, String.format("part number of this part (%04X):", fileHeader.getDisk()), String.valueOf(fileHeader.getDisk() + 1));
        printLine(out, "relative offset of local header:", String.format("%1$d (0x%1$08X) bytes", fileHeader.getLocalFileHeaderOffs()));
    }

    private void printVersion(PrintStream out) {
        VersionView.builder()
                   .versionMadeBy(fileHeader.getVersionMadeBy())
                   .versionToExtract(fileHeader.getVersionToExtract())
                   .offs(offs)
                   .columnWidth(columnWidth).build().print(out);
    }

    private void printGeneralPurposeFlag(PrintStream out) {
        GeneralPurposeFlagView.builder()
                              .generalPurposeFlag(fileHeader.getGeneralPurposeFlag())
                              .compressionMethod(fileHeader.getCompressionMethod())
                              .offs(offs)
                              .columnWidth(columnWidth).build().print(out);
    }

    private void printCompressionMethod(PrintStream out) {
        CompressionMethodView.builder()
                             .compressionMethod(fileHeader.getCompressionMethod())
                             .generalPurposeFlag(fileHeader.getGeneralPurposeFlag())
                             .offs(offs)
                             .columnWidth(columnWidth).build().print(out);
    }

    private void printLastModifiedTime(PrintStream out) {
        LastModifiedTimeView.builder()
                            .lastModifiedTime(fileHeader.getLastModifiedTime())
                            .offs(offs)
                            .columnWidth(columnWidth).build().print(out);
    }

    private void printCrc(PrintStream out) {
        printLine(out, "32-bit CRC value:", String.format("0x%08X", fileHeader.getCrc32()));
    }

    private void printSize(PrintStream out) {
        printLine(out, "compressed size:", String.valueOf(fileHeader.getCompressedSize()));
        printLine(out, "uncompressed size:", String.valueOf(fileHeader.getUncompressedSize()));
    }

    private void printFileName(PrintStream out) {
        printLine(out, "length of filename:", String.valueOf(fileHeader.getFileName().length()));

        StringHexView.builder()
                     .str(fileHeader.getFileName())
                     .charset(charset)
                     .offs(offs)
                     .columnWidth(columnWidth).build().print(out);
    }

    private void printComment(PrintStream out) {
        String comment = Optional.ofNullable(fileHeader.getComment()).orElse("");

        printLine(out, "length of file comment:", String.format("%d bytes", comment.getBytes(charset).length));

        StringHexView.builder()
                     .str(fileHeader.getComment())
                     .charset(charset)
                     .offs(offs)
                     .columnWidth(columnWidth).build().print(out);
    }

    private void printInternalFileAttributesView(PrintStream out) {
        InternalFileAttributesView.builder()
                                  .internalFileAttributes(fileHeader.getInternalFileAttributes())
                                  .offs(offs)
                                  .columnWidth(columnWidth).build().print(out);
    }

    private void printExternalFileAttributes(PrintStream out) {
        ExternalFileAttributesView.builder()
                                  .externalFileAttributes(fileHeader.getExternalFileAttributes())
                                  .offs(offs)
                                  .columnWidth(columnWidth).build().print(out);
    }

    private void printExtraField(PrintStream out) {
        ExtraFieldView.builder()
                      .extraField(fileHeader.getExtraField())
                      .diagExtraField(diagFileHeader.getExtraField())
                      .generalPurposeFlag(fileHeader.getGeneralPurposeFlag())
                      .offs(offs)
                      .columnWidth(columnWidth).build().print(out);
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Builder {

        private CentralDirectory.FileHeader fileHeader;
        private Diagnostic.CentralDirectory.FileHeader diagFileHeader = Diagnostic.CentralDirectory.FileHeader.NULL;
        private long pos;
        private Charset charset = Charsets.IBM437;
        private int offs;
        private int columnWidth;

        public FileHeaderView build() {
            return new FileHeaderView(this);
        }

        public Builder fileHeader(CentralDirectory.FileHeader fileHeader) {
            this.fileHeader = fileHeader;
            return this;
        }

        public Builder diagFileHeader(Diagnostic.CentralDirectory.FileHeader diagFileHeader) {
            this.diagFileHeader = Optional.ofNullable(diagFileHeader).orElse(Diagnostic.CentralDirectory.FileHeader.NULL);
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
