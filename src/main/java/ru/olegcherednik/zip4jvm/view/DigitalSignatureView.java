package ru.olegcherednik.zip4jvm.view;

import lombok.Builder;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.diagnostic.Block;

import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.stream.IntStream;

/**
 * @author Oleg Cherednik
 * @since 18.10.2019
 */
@Builder
public class DigitalSignatureView {

    private final CentralDirectory.DigitalSignature digitalSignature;
    private final Block block;
    private final Charset charset;
    private final String prefix;

    public void print(PrintStream out) {
        if (digitalSignature == null)
            return;

        String str = String.format("Digital signature %s: %d bytes", ViewUtils.signature(CentralDirectory.DigitalSignature.SIGNATURE),
                block.getSize());
        out.println(str);

        IntStream.range(0, str.length()).forEach(i -> out.print('='));

        out.println();
        out.format("%slocation of digital-signature record:           %2$d (0x%2$08X) bytes\n", prefix, block.getOffs());

        ByteArrayHexView.builder()
                        .buf(digitalSignature.getSignatureData())
                        .prefix(prefix).build().print(out);
    }

}
