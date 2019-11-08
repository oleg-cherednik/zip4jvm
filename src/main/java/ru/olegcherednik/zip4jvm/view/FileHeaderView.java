package ru.olegcherednik.zip4jvm.view;

import lombok.Builder;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.block.Diagnostic;
import ru.olegcherednik.zip4jvm.view.extrafield.ExtraFieldView;

import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * @author Oleg Cherednik
 * @since 14.10.2019
 */
@Builder
public class FileHeaderView {

    private final CentralDirectory.FileHeader fileHeader;
    private final Diagnostic.CentralDirectory.FileHeader diagFileHeader;
    private final long pos;
    private final Charset charset;
    private final String prefix;

    public void print(PrintStream out) {
        String str = String.format("Central directory entry %s #%d: %d bytes",
                ViewUtils.signature(CentralDirectory.FileHeader.SIGNATURE), pos + 1, diagFileHeader.getSize());
        out.println(str);

        IntStream.range(0, str.length()).forEach(i -> out.print('='));

        out.println();

        out.format("%sfilename (%s): %s\n", prefix, charset.name(), fileHeader.getFileName());
        out.format("%slocation of central-directory-record:           %2$d (0x%2$08X) bytes\n", prefix, diagFileHeader.getOffs());
        out.format("%spart number of this part (%04X):                %d\n", prefix, fileHeader.getDisk(), fileHeader.getDisk() + 1);
        out.format("%srelative offset of local header:                %2$d (0x%2$08X) bytes\n", prefix, fileHeader.getLocalFileHeaderOffs());

        printVersion(out);
        printGeneralPurposeFlag(out);
        printCompressionMethod(out);
        printLastModifiedTime(out);

        out.format("%s32-bit CRC value:                               0x%2$08X\n", prefix, fileHeader.getCrc32());
        out.format("%scompressed size:                                %d bytes\n", prefix, fileHeader.getCompressedSize());
        out.format("%suncompressed size:                              %d bytes\n", prefix, fileHeader.getUncompressedSize());
        out.format("%slength of filename:                             %d bytes\n", prefix,
                Optional.ofNullable(fileHeader.getFileName()).orElse("").getBytes(charset).length);
        StringHexView.builder()
                     .str(fileHeader.getFileName())
                     .charset(charset)
                     .prefix(prefix).build().print(out);

        out.format("%slength of file comment:                         %d bytes\n",
                prefix, Optional.ofNullable(fileHeader.getComment()).orElse("").getBytes(charset).length);
        StringHexView.builder()
                     .str(fileHeader.getComment())
                     .charset(charset)
                     .prefix(prefix).build().print(out);

        InternalFileAttributesView.builder()
                                  .internalFileAttributes(fileHeader.getInternalFileAttributes())
                                  .prefix(prefix).build().print(out);
        ExternalFileAttributesView.builder()
                                  .externalFileAttributes(fileHeader.getExternalFileAttributes())
                                  .prefix(prefix).build().print(out);

        printExtraField(out);
    }

    private void printVersion(PrintStream out) {
        VersionView.builder()
                   .versionMadeBy(fileHeader.getVersionMadeBy())
                   .versionToExtract(fileHeader.getVersionToExtract())
                   .offs(prefix.length())
                   .columnWidth(52).build().print(out);
    }

    private void printGeneralPurposeFlag(PrintStream out) {
        GeneralPurposeFlagView.builder()
                              .generalPurposeFlag(fileHeader.getGeneralPurposeFlag())
                              .compressionMethod(fileHeader.getCompressionMethod())
                              .offs(prefix.length())
                              .columnWidth(52).build().print(out);
    }

    private void printCompressionMethod(PrintStream out) {
        CompressionMethodView.builder()
                             .compressionMethod(fileHeader.getCompressionMethod())
                             .generalPurposeFlag(fileHeader.getGeneralPurposeFlag())
                             .offs(prefix.length())
                             .columnWidth(52).build().print(out);
    }

    private void printLastModifiedTime(PrintStream out) {
        LastModifiedTimeView.builder()
                            .lastModifiedTime(fileHeader.getLastModifiedTime())
                            .prefix(prefix).build().print(out);
    }

    private void printExtraField(PrintStream out) {
        ExtraFieldView.builder()
                      .extraField(fileHeader.getExtraField())
                      .diagExtraField(diagFileHeader.getExtraField())
                      .generalPurposeFlag(fileHeader.getGeneralPurposeFlag())
                      .offs(prefix.length())
                      .columnWidth(52).build().print(out);
    }

}
