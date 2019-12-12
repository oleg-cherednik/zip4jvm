package ru.olegcherednik.zip4jvm.engine.decompose;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.block.crypto.AesEncryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.model.block.crypto.EncryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.model.block.crypto.PkwareEncryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;
import ru.olegcherednik.zip4jvm.view.crypto.EncryptionHeaderView;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 09.12.2019
 */
@RequiredArgsConstructor
final class EncryptionHeaderDecompose {

    private final ZipModel zipModel;
    private final ZipInfoSettings settings;
    private final Encryption encryption;
    private final EncryptionHeaderBlock encryptionHeaderBlock;
    private final long pos;

    public boolean printTextInfo(PrintStream out, boolean emptyLine) {
        if (encryptionHeaderBlock != null)
            return encryptionHeaderView().print(out, emptyLine);

        return emptyLine;
    }

    public void decompose(Path dir) throws IOException {
        if (encryptionHeaderBlock == null)
            return;

        Path subDir = Files.createDirectories(dir.resolve("encryption"));


        // TODO probably same with block reader
        if (encryption == Encryption.AES_128 || encryption == Encryption.AES_192 || encryption == Encryption.AES_256) {
            AesEncryptionHeaderBlock block = (AesEncryptionHeaderBlock)encryptionHeaderBlock;
            DecomposeUtils.print(subDir.resolve("aes_encryption_header.txt"), out -> encryptionHeaderView().print(out));

            DecomposeUtils.copyLarge(zipModel, subDir.resolve("aes_salt.data"), block.getSalt());
            DecomposeUtils.copyLarge(zipModel, subDir.resolve("aes_password_checksum.data"), block.getPasswordChecksum());
            DecomposeUtils.copyLarge(zipModel, subDir.resolve("aes_mac.data"), block.getMac());
        } else if (encryption == Encryption.PKWARE) {
            PkwareEncryptionHeaderBlock block = (PkwareEncryptionHeaderBlock)encryptionHeaderBlock;
            DecomposeUtils.print(dir.resolve("pkware_encryption_header.txt"), out -> encryptionHeaderView().print(out));
            DecomposeUtils.copyLarge(zipModel, subDir.resolve("pkware_encryption_header.data"), block.getHeader());
        } else {
            // TODO print unknown header
        }
    }

    private EncryptionHeaderView encryptionHeaderView() {
        Function<Block, byte[]> getDataFunc = DecomposeUtils.getDataFunc(zipModel);
        return new EncryptionHeaderView(encryptionHeaderBlock, getDataFunc, pos, settings.getOffs(), settings.getColumnWidth());
    }
}
