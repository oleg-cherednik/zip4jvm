package ru.olegcherednik.zip4jvm.view.centraldirectory;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.Charsets;
import ru.olegcherednik.zip4jvm.model.CompressionMethod;
import ru.olegcherednik.zip4jvm.model.ExtraField;
import ru.olegcherednik.zip4jvm.model.GeneralPurposeFlag;
import ru.olegcherednik.zip4jvm.model.block.CentralDirectoryBlock;
import ru.olegcherednik.zip4jvm.view.BaseView;
import ru.olegcherednik.zip4jvm.view.CompressionMethodView;
import ru.olegcherednik.zip4jvm.view.ExternalFileAttributesView;
import ru.olegcherednik.zip4jvm.view.GeneralPurposeFlagView;
import ru.olegcherednik.zip4jvm.view.InternalFileAttributesView;
import ru.olegcherednik.zip4jvm.view.LastModifiedTimeView;
import ru.olegcherednik.zip4jvm.view.SizeView;
import ru.olegcherednik.zip4jvm.view.StringHexView;
import ru.olegcherednik.zip4jvm.view.VersionView;
import ru.olegcherednik.zip4jvm.view.extrafield.ExtraFieldView;

import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.Optional;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireNotNull;

/**
 * @author Oleg Cherednik
 * @since 14.10.2019
 */
public final class FileHeaderView extends BaseView {

    private final CentralDirectory.FileHeader fileHeader;
    private final CentralDirectoryBlock.FileHeaderBlock block;
    private final long pos;
    private final Charset charset;

    public static Builder builder() {
        return new Builder();
    }

    private FileHeaderView(Builder builder) {
        super(builder.offs, builder.columnWidth, builder.totalDisks);
        fileHeader = builder.fileHeader;
        block = builder.block;
        pos = builder.pos;
        charset = builder.charset;
    }

    @Override
    public boolean print(PrintStream out) {
        printSubTitle(out, CentralDirectory.FileHeader.SIGNATURE, pos, '[' + charset.name() + "] " + fileHeader.getFileName(), block);
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
        printLine(out, String.format("part number of this part (%04X):", fileHeader.getDiskNo()), String.valueOf(fileHeader.getDiskNo() + 1));
        printLine(out, "relative offset of local header:", String.format("%1$d (0x%1$08X) bytes", fileHeader.getLocalFileHeaderRelativeOffs()));
    }

    private void printVersion(PrintStream out) {
        new VersionView(fileHeader.getVersionMadeBy(), fileHeader.getVersionToExtract(), offs, columnWidth).print(out);
    }

    private void printGeneralPurposeFlag(PrintStream out) {
        new GeneralPurposeFlagView(fileHeader.getGeneralPurposeFlag(), fileHeader.getCompressionMethod(), offs, columnWidth).print(out);
    }

    private void printCompressionMethod(PrintStream out) {
        CompressionMethod compressionMethod = fileHeader.getCompressionMethod();
        GeneralPurposeFlag generalPurposeFlag = fileHeader.getGeneralPurposeFlag();
        new CompressionMethodView(compressionMethod, generalPurposeFlag, offs, columnWidth).print(out);
    }

    private void printLastModifiedTime(PrintStream out) {
        new LastModifiedTimeView(fileHeader.getLastModifiedTime(), offs, columnWidth).print(out);
    }

    private void printCrc(PrintStream out) {
        printLine(out, "32-bit CRC value:", String.format("0x%08X", fileHeader.getCrc32()));
    }

    private void printSize(PrintStream out) {
        new SizeView("compressed size:", fileHeader.getCompressedSize(), offs, columnWidth).print(out);
        new SizeView("uncompressed size:", fileHeader.getUncompressedSize(), offs, columnWidth).print(out);
    }

    private void printFileName(PrintStream out) {
        printLine(out, "length of filename:", String.valueOf(fileHeader.getFileName().length()));
        new StringHexView(fileHeader.getFileName(), charset, offs, columnWidth).print(out);
    }

    private void printComment(PrintStream out) {
        String comment = Optional.ofNullable(fileHeader.getComment()).orElse("");
        printLine(out, "length of file comment:", String.format("%d bytes", comment.getBytes(charset).length));
        new StringHexView(fileHeader.getComment(), charset, offs, columnWidth).print(out);
    }

    private void printInternalFileAttributesView(PrintStream out) {
        new InternalFileAttributesView(fileHeader.getInternalFileAttributes(), offs, columnWidth).print(out);
    }

    private void printExternalFileAttributes(PrintStream out) {
        new ExternalFileAttributesView(fileHeader.getExternalFileAttributes(), offs, columnWidth).print(out);
    }

    private void printExtraField(PrintStream out) {
        if (fileHeader.getExtraField() == ExtraField.NULL)
            return;

        ExtraFieldView.builder()
                      .extraField(fileHeader.getExtraField())
                      .block(block.getExtraFieldBlock())
                      .generalPurposeFlag(fileHeader.getGeneralPurposeFlag())
                      .position(offs, columnWidth, totalDisks).build().printLocation(out);
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Builder {

        private CentralDirectory.FileHeader fileHeader;
        private CentralDirectoryBlock.FileHeaderBlock block;
        private long pos;
        private Charset charset = Charsets.IBM437;
        private int offs;
        private int columnWidth;
        private long totalDisks;

        public FileHeaderView build() {
            requireNotNull(fileHeader, "FileHeaderView.fileHeader");
            requireNotNull(block, "FileHeaderView.block");
            return new FileHeaderView(this);
        }

        public Builder fileHeader(CentralDirectory.FileHeader fileHeader) {
            this.fileHeader = fileHeader;
            return this;
        }

        public Builder block(CentralDirectoryBlock.FileHeaderBlock block) {
            this.block = block;
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
