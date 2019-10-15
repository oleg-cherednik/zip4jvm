package ru.olegcherednik.zip4jvm.view;

import lombok.Builder;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;

import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * @author Oleg Cherednik
 * @since 14.10.2019
 */
@Builder
public class FileHeaderView {

    private final long offs;
    private final long size;
    private final long pos;
    private final CentralDirectory.FileHeader fileHeader;
    private final Charset charset;
    private final String prefix;

    public void print(PrintStream out) {
        String str = String.format("Central directory entry %s #%d: %d bytes",
                ViewUtils.signature(CentralDirectory.FileHeader.SIGNATURE), pos + 1, size);
        out.println(str);

        IntStream.range(0, str.length()).forEach(i -> out.print('='));

        out.println();
        out.format("%sfilename (%s): %s\n", prefix, charset.name(), fileHeader.getFileName());
        out.format("%slocation of central-directory-record:           %2$d (0x%2$08X) bytes\n", prefix, offs);
        out.format("%spart number of this part (%04X):                %d\n", prefix, fileHeader.getDisk(), fileHeader.getDisk() + 1);
        out.format("%srelative offset of local header:                %2$d (0x%2$08X) bytes\n", prefix, fileHeader.getLocalFileHeaderOffs());
        out.format(Locale.US, "%sversion made by zip software (%02d):              %.1f\n",
                prefix, fileHeader.getVersionMadeBy(), (double)fileHeader.getVersionMadeBy() / 10);
        out.format(Locale.US, "%sunzip software version needed to extract (%02d):  %.1f\n",
                prefix, fileHeader.getVersionToExtract(), (double)fileHeader.getVersionToExtract() / 10);

        GeneralPurposeFlagView.builder()
                              .generalPurposeFlag(fileHeader.getGeneralPurposeFlag())
                              .compressionMethod(fileHeader.getCompressionMethod())
                              .prefix(prefix).build().print(out);
        CompressionMethodView.builder()
                             .compressionMethod(fileHeader.getCompressionMethod())
                             .generalPurposeFlag(fileHeader.getGeneralPurposeFlag())
                             .prefix(prefix).build().print(out);
        LastModifiedTimeView.builder()
                            .lastModifiedTime(fileHeader.getLastModifiedTime())
                            .prefix(prefix).build().print(out);

        out.format("%s32-bit CRC value:                               0x%2$08X\n", prefix, fileHeader.getCrc32());
        out.format("%scompressed size:                                %d bytes\n", prefix, fileHeader.getCompressedSize());
        out.format("%suncompressed size:                              %d bytes\n", prefix, fileHeader.getUncompressedSize());
        out.format("%slength of filename:                             %d bytes\n", prefix,
                Optional.ofNullable(fileHeader.getFileName()).orElse("").getBytes(charset).length);
        StringHexView.builder()
                     .str(fileHeader.getFileName())
                     .charset(charset)
                     .prefix(prefix).build().print(out);

//        length of extra field:                          0 bytes
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

        //        internal file attributes:                       0x0000
//        apparent file type:                           binary
//        external file attributes:                       0x00000020
//        non-MSDOS external file attributes:           0x000000
//        MS-DOS file attributes (0x20):                arc


        int a = 0;
        a++;
    }

}
