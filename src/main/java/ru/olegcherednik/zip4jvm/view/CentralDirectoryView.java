package ru.olegcherednik.zip4jvm.view;

import lombok.Builder;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;

import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * @author Oleg Cherednik
 * @since 14.10.2019
 */
@Builder
public class CentralDirectoryView {

    private final long offs;
    private final long size;
    private final Map<String, Long> fileHeaderOffs;
    private final Map<String, Long> fileHeaderSize;
    private final Map<String, Long> fileHeaderExternalFieldOffs;
    private final Map<String, Long> fileHeaderExternalFieldSize;
    private final CentralDirectory centralDirectory;
    private final long digitalSignatureOffs;
    private final long getDigitalSignatureSize;
    private final Charset charset;
    private final String prefix;

    public void print(PrintStream out) {
        if (centralDirectory == null)
            return;

        String str = String.format("Central directory %s: %d bytes", ViewUtils.signature(CentralDirectory.FileHeader.SIGNATURE), size);
        out.println(str);

        IntStream.range(0, str.length()).forEach(i -> out.print('='));

        out.println();
        out.format("%slocation of central-dir record:                 %2$d (0x%2$08X) bytes\n", prefix, offs);
        out.format("%stotal number of entries in central dir:         %d\n", prefix, centralDirectory.getFileHeaders().size());

        out.println();
        printFileHeaders(out);
        out.println();
        DigitalSignatureView.builder()
                            .offs(digitalSignatureOffs)
                            .size(getDigitalSignatureSize)
                            .digitalSignature(centralDirectory.getDigitalSignature())
                            .charset(charset)
                            .prefix(prefix).build().print(out);
    }

    private void printFileHeaders(PrintStream out) {
        int pos = 0;

        for (CentralDirectory.FileHeader fileHeader : centralDirectory.getFileHeaders()) {
            if (pos != 0)
                out.println();

            FileHeaderView.builder()
                          .offs(fileHeaderOffs.get(fileHeader.getFileName()))
                          .size(fileHeaderSize.get(fileHeader.getFileName()))
                          .extraFieldOffs(fileHeaderExternalFieldOffs.getOrDefault(fileHeader.getFileName(), -1L))
                          .pos(pos++)
                          .fileHeader(fileHeader)
                          .charset(charset)
                          .prefix(prefix).build().print(out);
        }
    }

}
