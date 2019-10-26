package ru.olegcherednik.zip4jvm.view;

import lombok.Builder;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.block.Diagnostic;

import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.stream.IntStream;

/**
 * @author Oleg Cherednik
 * @since 14.10.2019
 */
@Builder
public class CentralDirectoryView {

    private final CentralDirectory centralDirectory;
    private final Diagnostic.CentralDirectory diagCentralDirectory;
    private final Charset charset;
    private final String prefix;

    public void print(PrintStream out) {
        if (centralDirectory == null)
            return;

        String str = String.format("Central directory %s: %d bytes", ViewUtils.signature(CentralDirectory.FileHeader.SIGNATURE),
                diagCentralDirectory.getSize());
        out.println(str);

        IntStream.range(0, str.length()).forEach(i -> out.print('='));

        out.println();
        out.format("%slocation of central-dir record:                 %2$d (0x%2$08X) bytes\n", prefix, diagCentralDirectory.getOffs());
        out.format("%stotal number of entries in central dir:         %d\n", prefix, centralDirectory.getFileHeaders().size());

        out.println();
        printFileHeaders(out);

        out.println();
        DigitalSignatureView.builder()
                            .digitalSignature(centralDirectory.getDigitalSignature())
                            .block(diagCentralDirectory.getDigitalSignature())
                            .charset(charset)
                            .prefix(prefix).build().print(out);
    }

    private void printFileHeaders(PrintStream out) {
        int pos = 0;

        for (CentralDirectory.FileHeader fileHeader : centralDirectory.getFileHeaders()) {
            if (pos != 0)
                out.println();

            FileHeaderView.builder()
                          .fileHeader(fileHeader)
                          .diagFileHeader(diagCentralDirectory.getFileHeader(fileHeader.getFileName()))
                          .pos(pos++)
                          .charset(charset)
                          .prefix(prefix).build().print(out);
        }
    }

}
