package ru.olegcherednik.zip4jvm.view;

import lombok.Builder;
import ru.olegcherednik.zip4jvm.model.LocalFileHeader;
import ru.olegcherednik.zip4jvm.model.block.Diagnostic;

import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * @author Oleg Cherednik
 * @since 23.10.2019
 */
@Builder
public class LocalFileHeaderView {

    private final LocalFileHeader localFileHeader;
    private final Diagnostic.ZipEntryBlock.LocalFileHeader diagLocalFileHeader;
    private final Diagnostic.ZipEntryBlock.EncryptionHeader diagEncryptionHeader;
    private final long pos;
    private final Charset charset;
    private final String prefix;

    public void print(PrintStream out) {
        String str = String.format("Local directory entry %s #%d: %d bytes",
                ViewUtils.signature(LocalFileHeader.SIGNATURE), pos + 1, diagLocalFileHeader.getSize());
        out.println(str);

        IntStream.range(0, str.length()).forEach(i -> out.print('='));

        out.println();

        out.format("%sfilename (%s): %s\n", prefix, charset.name(), localFileHeader.getFileName());
        out.format("%slocation:                                       %2$d (0x%2$08X) bytes\n", prefix, diagLocalFileHeader.getOffs());
        out.format("%spart number (%04X):                             %d\n", prefix, diagLocalFileHeader.getDisk(),
                diagLocalFileHeader.getDisk() + 1);
        out.format("%soffset:                                         %2$d (0x%2$08X) bytes\n", prefix, diagLocalFileHeader.getOffs());

//        VersionView.builder()
//                   .versionMadeBy(localFileHeader.getVersionToExtract()getVersionMadeBy())
//                   .versionToExtract(localFileHeader.getVersionToExtract())
//                   .prefix(prefix).build().print(out);

        GeneralPurposeFlagView.builder()
                              .generalPurposeFlag(localFileHeader.getGeneralPurposeFlag())
                              .compressionMethod(localFileHeader.getCompressionMethod())
                              .prefix(prefix).build().print(out);
        CompressionMethodView.builder()
                             .compressionMethod(localFileHeader.getCompressionMethod())
                             .generalPurposeFlag(localFileHeader.getGeneralPurposeFlag())
                             .prefix(prefix).build().print(out);
        LastModifiedTimeView.builder()
                            .lastModifiedTime(localFileHeader.getLastModifiedTime())
                            .prefix(prefix).build().print(out);

        out.format("%s32-bit CRC value:                               0x%2$08X\n", prefix, localFileHeader.getCrc32());
        out.format("%scompressed size:                                %d bytes\n", prefix, localFileHeader.getCompressedSize());
        out.format("%suncompressed size:                              %d bytes\n", prefix, localFileHeader.getUncompressedSize());
        out.format("%slength of filename:                             %d bytes\n", prefix,
                Optional.ofNullable(localFileHeader.getFileName()).orElse("").getBytes(charset).length);
        StringHexView.builder()
                     .str(localFileHeader.getFileName())
                     .charset(charset)
                     .prefix(prefix).build().print(out);

        ExtraFieldView.builder()
                      .extraField(localFileHeader.getExtraField())
                      .diagExtraField(diagLocalFileHeader.getExtraField())
                      .generalPurposeFlag(localFileHeader.getGeneralPurposeFlag())
                      .prefix(prefix).build().print(out);

    }

}
