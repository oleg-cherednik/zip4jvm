package ru.olegcherednik.zip4jvm.io.readers;

import ru.olegcherednik.zip4jvm.crypto.strong.EncryptionAlgorithm;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.model.DecryptionHeader;
import ru.olegcherednik.zip4jvm.utils.function.Reader;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 11.10.2019
 */
public final class DecryptionHeaderReader implements Reader<DecryptionHeader> {

    @Override
    public DecryptionHeader read(DataInput in) throws IOException {
        DecryptionHeader header = new DecryptionHeader();

        header.setIvSize(in.readWord());
        header.setIv(in.readBytes(header.getIvSize()));
        header.setSize(in.readDword());

        long offs = in.getOffs();

        header.setFormat(in.readWord());
        header.setEncryptionAlgorithm(EncryptionAlgorithm.parseCode(in.readWord()));
        header.setBitLength(in.readWord());
        header.setFlags(in.readWord());
        header.setEncryptedRandomDataSize(in.readWord());
        header.setEncryptedRandomData(in.readBytes(header.getEncryptedRandomDataSize()));

        in.skip(4);

        header.setPasswordValidationDataSize(in.readWord());
        header.setPasswordValidationData(in.readBytes(header.getPasswordValidationDataSize() - 4));
        header.setCrc32(in.readDword());

        if (in.getOffs() - offs != header.getSize())
            throw new Zip4jvmException("DecryptionHeader size is incorrect");

        return header;
    }
}
