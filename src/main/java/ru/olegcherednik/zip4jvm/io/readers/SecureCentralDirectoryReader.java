package ru.olegcherednik.zip4jvm.io.readers;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.crypto.Decoder;
import ru.olegcherednik.zip4jvm.crypto.strong.EncryptionAlgorithm;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.in.BaseDataInput;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.utils.function.Reader;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 10.10.2019
 */
@RequiredArgsConstructor
final class SecureCentralDirectoryReader implements Reader<CentralDirectory> {

    private final long offs;
    private final long totalEntries;
    private final Function<Charset, Charset> charsetCustomizer;
    private final Zip64.ExtensibleDataSector extensibleDataSector;

    @Override
    public CentralDirectory read(DataInput in) throws IOException {
        findHead(in);
        Decoder decoder = getEncryption().getCreateDecoderCentral().apply(extensibleDataSector, in);

        try (DataInput inn = new BaseDataInput() {
            @Override
            public int read(byte[] buf, int offs, int len) throws IOException {
                return 0;
            }
        }) {
            CentralDirectory dir = new CentralDirectoryReader(inn.getOffs(), totalEntries, charsetCustomizer).read(inn);
            return dir;
        }
//        try (InputStream is = new CentralDirectoryInflateInputStream()) {
//
//        }

    }

    private void findHead(DataInput in) throws IOException {
        in.seek(offs);
    }

    private Encryption getEncryption() {
        EncryptionAlgorithm encryptionAlgorithm = extensibleDataSector.getEncryptionAlgorithm();

        if (encryptionAlgorithm == EncryptionAlgorithm.AES_128)
            return Encryption.AES_128;
        if (encryptionAlgorithm == EncryptionAlgorithm.AES_192)
            return Encryption.AES_192;
        if (encryptionAlgorithm == EncryptionAlgorithm.AES_256)
            return Encryption.AES_256;

        throw new Zip4jvmException("Encryption algorithm is not supported: " + encryptionAlgorithm);
    }
}
