package ru.olegcherednik.zip4jvm.view;

import lombok.Builder;
import ru.olegcherednik.zip4jvm.io.readers.block.aes.AesEncryptionHeader;
import ru.olegcherednik.zip4jvm.model.block.Diagnostic;

import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.stream.IntStream;

/**
 * @author Oleg Cherednik
 * @since 24.10.2019
 */
@Builder
public class EncryptionHeaderView {

    private final Diagnostic.ZipEntryBlock.EncryptionHeader encryptionHeader;
    private final long pos;
    private final Charset charset;
    private final String prefix;

    public void print(PrintStream out) {
        if (encryptionHeader == null)
            return;

        String str = String.format("#%d (%s) Encryption header - %d bytes", pos + 1, "11", 1);
        out.println(str);

        IntStream.range(0, str.length()).forEach(i -> out.print('='));

        out.println();

        if (encryptionHeader instanceof AesEncryptionHeader)
            print((AesEncryptionHeader)encryptionHeader, out);

        int a = 0;
        a++;
    }

    private void print(AesEncryptionHeader encryptionHeader, PrintStream out) {
        out.format("%sEncryption header AES \n", prefix);

        out.format("%s  salt: %2$d (0x%2$08X) bytes\n", prefix, encryptionHeader.getSalt().getOffs());
        out.format("%s  - size: %d bytes\n", prefix, encryptionHeader.getSalt().getSize());
        ByteArrayHexView.builder()
                        .buf(encryptionHeader.getSalt().getData())
                        .prefix(prefix).build().print(out);
        int a = 0;
        a++;
    }
}
