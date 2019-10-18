package ru.olegcherednik.zip4jvm.view;

import lombok.Builder;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;

import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.stream.IntStream;

/**
 * @author Oleg Cherednik
 * @since 18.10.2019
 */
@Builder
public class DigitalSignatureView {

    private final long offs;
    private final long size;
    private final CentralDirectory.DigitalSignature digitalSignature;
    private final Charset charset;
    private final String prefix;

    public void print(PrintStream out) {
        if (digitalSignature == null)
            return;

        String str = String.format("Digital signature %s: %d bytes", ViewUtils.signature(CentralDirectory.DigitalSignature.SIGNATURE),
                digitalSignature.getSignatureData().length);
        out.println(str);

        IntStream.range(0, str.length()).forEach(i -> out.print('='));

        out.println();
        out.format("%slocation of digital-signature record:           %2$d (0x%2$08X) bytes\n", prefix, offs);

        ByteArrayHexView.builder()
                        .buf(digitalSignature.getSignatureData())
                        .prefix(prefix).build().print(out);
    }

}
