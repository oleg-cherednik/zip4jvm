package ru.olegcherednik.zip4jvm.engine.decompose;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.io.readers.block.aes.AesEncryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.io.readers.block.pkware.PkwareEncryptionHeader;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.block.ZipEntryBlock;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;
import ru.olegcherednik.zip4jvm.view.crypto.EncryptionHeaderView;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 09.12.2019
 */
@RequiredArgsConstructor
final class EncryptionHeaderDecompose {

    private final ZipModel zipModel;
    private final ZipInfoSettings settings;
    private final Encryption encryption;
    private final ZipEntryBlock.EncryptionHeader encryptionHeader;
    private final long pos;

    public boolean printTextInfo(PrintStream out, boolean emptyLine) {
        if (encryptionHeader != null)
            return new EncryptionHeaderView(encryptionHeader, pos, settings.getOffs(), settings.getColumnWidth()).print(out, emptyLine);

        return emptyLine;
    }

    public void decompose(Path dir) throws IOException {
        if (encryptionHeader == null)
            return;

        Path subDir = Files.createDirectories(dir.resolve("encryption"));


        // TODO probably same with block reader
        if (encryption == Encryption.AES_128 || encryption == Encryption.AES_192 || encryption == Encryption.AES_256) {
            AesEncryptionHeaderBlock block = (AesEncryptionHeaderBlock)encryptionHeader;
            Utils.print(subDir.resolve("aes_encryption_header.txt"), out -> encryptionHeaderView().print(out));

            Utils.copyLarge(zipModel, subDir.resolve("aes_salt.data"), block.getSalt());
            Utils.copyLarge(zipModel, subDir.resolve("aes_password_checksum.data"), block.getPasswordChecksum());
            Utils.copyLarge(zipModel, subDir.resolve("aes_mac.data"), block.getMac());
        } else if (encryption == Encryption.PKWARE) {
            PkwareEncryptionHeader block = (PkwareEncryptionHeader)encryptionHeader;
            Utils.print(dir.resolve("pkware_encryption_header.txt"), out -> encryptionHeaderView().print(out));
            Utils.copyLarge(zipModel, subDir.resolve("pkware_encryption_header.data"), block.getData());
        } else {
            // TODO print unknown header
        }
    }

    private EncryptionHeaderView encryptionHeaderView() {
        return new EncryptionHeaderView(encryptionHeader, pos, settings.getOffs(), settings.getColumnWidth());
    }
}
