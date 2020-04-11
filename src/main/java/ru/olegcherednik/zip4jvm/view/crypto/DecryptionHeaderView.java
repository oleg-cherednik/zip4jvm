package ru.olegcherednik.zip4jvm.view.crypto;

import org.apache.commons.lang.ArrayUtils;
import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeader;
import ru.olegcherednik.zip4jvm.crypto.strong.EncryptionAlgorithm;
import ru.olegcherednik.zip4jvm.crypto.strong.Flags;
import ru.olegcherednik.zip4jvm.crypto.strong.HashAlgorithm;
import ru.olegcherednik.zip4jvm.crypto.strong.Recipient;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.block.crypto.DecryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.view.BaseView;
import ru.olegcherednik.zip4jvm.view.ByteArrayHexView;
import ru.olegcherednik.zip4jvm.view.SizeView;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Optional;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireNotNull;

/**
 * @author Oleg Cherednik
 * @since 24.02.2020
 */
public class DecryptionHeaderView extends BaseView {

    private final DecryptionHeader decryptionHeader;
    private final DecryptionHeaderBlock block;
    private final long pos;

    protected DecryptionHeaderView(DecryptionHeader decryptionHeader, DecryptionHeaderBlock block, long pos, int offs, int columnWidth,
            long totalDisks) {
        super(offs, columnWidth, totalDisks);
        this.decryptionHeader = decryptionHeader;
        this.block = requireNotNull(block, "BlockDecryptionHeaderView.localFileHeader");
        this.pos = pos;

        Recipient one = new Recipient();
        one.setHash(new byte[] { 1, 2, 3 });
        one.setSimpleKeyBlob(new byte[] { 4, 5, 6 });
        one.setSize(6);

        Recipient two = new Recipient();
        two.setHash(new byte[] { 11, 22, 33 });
        two.setSimpleKeyBlob(new byte[] { 44, 55, 66 });
        two.setSize(66);

        decryptionHeader.setRecipients(Arrays.asList(one, two));

        block.getRecipientsBlock().setOffs(0x666);

        block.getRecipientsBlock().addRecipient(0, Block.NULL);
        block.getRecipientsBlock().addRecipient(1, Block.NULL);
    }

    @Override
    public boolean print(PrintStream out) {
        printSubTitle(out, pos, "(Strong) encryption header", block);
        printIv(out);
        printVersion(out);
        printEncryptionAlgorithm(out);
        printBitLength(out);
        printFlags(out);
        printEncryptedRandomData(out);
        printHashAlgorithm(out);
        printPasswordValidationData(out);
        printChecksum(out);
        printRecipients(out);
        return true;
    }

    private void printIv(PrintStream out) {
        new SizeView("iv:", decryptionHeader.getIv().length, offs, columnWidth).print(out);
        new ByteArrayHexView(decryptionHeader.getIv(), offs, columnWidth).print(out);
    }

    private void printVersion(PrintStream out) {
        printLine(out, "version:", decryptionHeader.getVersion());
    }

    private void printEncryptionAlgorithm(PrintStream out) {
        int code = decryptionHeader.getEncryptionAlgorithmCode();
        EncryptionAlgorithm encryptionAlgorithm = decryptionHeader.getEncryptionAlgorithm();
        printLine(out, String.format("encryption algorithm (0x%04X):", code), encryptionAlgorithm.getTitle());
    }

    private void printBitLength(PrintStream out) {
        printLine(out, "encryption key bits:", decryptionHeader.getBitLength());
    }

    private void printFlags(PrintStream out) {
        Flags flags = decryptionHeader.getFlags();
        printLine(out, String.format("flags (0x%02X):", flags.getCode()), flags.getTitle());
    }

    private void printEncryptedRandomData(PrintStream out) {
        byte[] encryptedRandomData = Optional.ofNullable(decryptionHeader.getEncryptedRandomData()).orElse(ArrayUtils.EMPTY_BYTE_ARRAY);
        printLine(out, "length of encrypted random data:", String.format("%d bytes", encryptedRandomData.length));
//        new ByteArrayHexView(encryptedRandomData, offs, columnWidth).print(out);
    }

    private void printHashAlgorithm(PrintStream out) {
        int code = decryptionHeader.getHashAlgorithmCode();
        HashAlgorithm hashAlgorithm = decryptionHeader.getHashAlgorithm();
        printLine(out, String.format("hash algorithm (0x%04X):", code), hashAlgorithm.getTitle());
    }

    private void printPasswordValidationData(PrintStream out) {
        byte[] passwordValidationData = Optional.ofNullable(decryptionHeader.getPasswordValidationData()).orElse(ArrayUtils.EMPTY_BYTE_ARRAY);
        printLine(out, "password validation data:", String.format("%d bytes", passwordValidationData.length));
//        new ByteArrayHexView(passwordValidationData, offs, columnWidth).print(out);
    }

    private void printChecksum(PrintStream out) {
        printLine(out, "crc32:", String.format("0x%1$08X", decryptionHeader.getCrc32()));
    }

    private void printRecipients(PrintStream out) {
        printValueWithLocation2(out, "recipients:", block, decryptionHeader.getRecipients().size());

        int i = 0;

        for (Recipient recipient : decryptionHeader.getRecipients()) {
            RecipientView.builder()
                         .num(i)
                         .recipient(recipient)
                         .block(block.getRecipientsBlock().getRecipient(i))
                         .position(offs + 2, columnWidth, totalDisks).build().print(out);
            i++;
        }
    }
}
