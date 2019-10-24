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

        if (encryptionHeader instanceof AesEncryptionHeader)
            print((AesEncryptionHeader)encryptionHeader, out);

        int a = 0;
        a++;
    }

    private void print(AesEncryptionHeader encryptionHeader, PrintStream out) {
        String str = String.format("#%d (AES) encryption header", pos + 1);
        out.println(str);

        IntStream.range(0, str.length()).forEach(i -> out.print('='));

        out.println();
        out.format("%ssalt:                                           %d bytes\n", prefix, encryptionHeader.getSalt().getSize());
        out.format("%s  - location:                                   %2$d (0x%2$08X) bytes\n", prefix, encryptionHeader.getSalt().getOffs());

        ByteArrayHexView.builder()
                        .buf(encryptionHeader.getSalt().getData())
                        .prefix(prefix).build().print(out);

        out.format("%spassword checksum:                              %d bytes\n",
                prefix, encryptionHeader.getPasswordChecksum().getSize());
        out.format("%s  - location:                                   %2$d (0x%2$08X) bytes\n",
                prefix, encryptionHeader.getPasswordChecksum().getOffs());

        ByteArrayHexView.builder()
                        .buf(encryptionHeader.getPasswordChecksum().getData())
                        .prefix(prefix).build().print(out);

        out.format("%smac:                                            %d bytes\n", prefix, encryptionHeader.getMac().getSize());
        out.format("%s  - location:                                   %2$d (0x%2$08X) bytes\n", prefix, encryptionHeader.getMac().getOffs());

        ByteArrayHexView.builder()
                        .buf(encryptionHeader.getMac().getData())
                        .prefix(prefix).build().print(out);
    }
}
