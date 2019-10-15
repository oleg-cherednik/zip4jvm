package ru.olegcherednik.zip4jvm.view;

import lombok.Builder;
import org.apache.commons.lang.StringUtils;
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
        out.format("%sfilename (%s):                               %s\n", prefix, charset.name(), fileHeader.getFileName());
        out.format("%slocation of central-directory-record:           %2$d (0x%2$08X) bytes\n", prefix, offs);
        out.format("%spart number of this part (%04X):                %d\n", prefix, fileHeader.getDisk(), fileHeader.getDisk() + 1);
        out.format("%srelative offset of local header:                %2$d (0x%2$08X) bytes\n", prefix, fileHeader.getLocalFileHeaderOffs());
        out.format(Locale.US, "%sversion made by zip software (%02d):              %.1f\n",
                prefix, fileHeader.getVersionMadeBy(), (double)fileHeader.getVersionMadeBy() / 10);
        out.format(Locale.US, "%sunzip software version needed to extract (%02d):  %.1f\n",
                prefix, fileHeader.getVersionToExtract(), (double)fileHeader.getVersionToExtract() / 10);

        GeneralPurposeFlagView.builder()
                              .generalPurposeFlag(fileHeader.getGeneralPurposeFlag())
                              .prefix(prefix).build().print(out);

//        compression method (08):                        deflated
//        compression sub-type (deflation):             normal
//        file last modified on (0x00004f3c 0x00005ae0):  2019-09-28 11:23:00

        out.format("%s32-bit CRC value:                               0x%2$08X\n", prefix, fileHeader.getCrc32());
        out.format("%scompressed size:                                %d bytes\n", prefix, fileHeader.getCompressedSize());
        out.format("%suncompressed size:                              %d bytes\n", prefix, fileHeader.getUncompressedSize());
        out.format("%slength of filename:                             %d bytes\n", prefix,
                Optional.ofNullable(fileHeader.getFileName()).orElse("").getBytes(charset).length);
//        length of extra field:                          0 bytes
        out.format("%slength of file comment:                         %d bytes\n",
                prefix, Optional.ofNullable(fileHeader.getComment()).orElse("").getBytes(charset).length);
//        internal file attributes:                       0x0000
//        apparent file type:                           binary
//        external file attributes:                       0x00000020
//        non-MSDOS external file attributes:           0x000000
//        MS-DOS file attributes (0x20):                arc


        if (StringUtils.isNotEmpty(fileHeader.getComment())) {
            StringHexView.builder()
                         .str(fileHeader.getComment())
                         .charset(charset)
                         .prefix(prefix).build().print(out);
        }

        int a = 0;
        a++;
    }

/*
compression method (01):                        shrunk
compression method (02):                        reduced (factor 1)
compression method (03):                        reduced (factor 2)
compression method (04):                        reduced (factor 3)
compression method (05):                        reduced (factor 4)

compression method (06):                        imploded
  size of sliding dictionary (implosion):       4K
  number of Shannon-Fano trees (implosion):     2
  size of sliding dictionary (implosion):       8K
  number of Shannon-Fano trees (implosion):     3

compression method (08):                        deflated

compression method (09):                        deflated (enhanced)
  compression sub-type (deflation):             normal
  compression sub-type (deflation):             maximum
  compression sub-type (deflation):             superfast
  compression sub-type (deflation):             fast

compression method (12):                        bzip2 algorithm

compression method (14):                        lzma encoding
  end-of-stream (EOS) marker:                   no
  end-of-stream (EOS) marker:                   yes

compression method (96):                        jpeg compression
compression method (97):                        wavpack compression
compression method (98):                        ppmd encoding
compression method (99):                        AES encryption
 */
}
