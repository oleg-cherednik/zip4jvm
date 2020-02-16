package ru.olegcherednik.zip4jvm.io.readers.extrafiled;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.crypto.strong.EncryptionAlgorithm;
import ru.olegcherednik.zip4jvm.crypto.strong.Flags;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.model.extrafield.AlgIdExtraFieldRecord;
import ru.olegcherednik.zip4jvm.utils.function.Reader;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 15.02.2020
 */
@RequiredArgsConstructor
public final class AlgIdExtraFieldRecordReader implements Reader<AlgIdExtraFieldRecord> {

    private final int size;

    @Override
    public AlgIdExtraFieldRecord read(DataInput in) throws IOException {
        int format = in.readWord();
        EncryptionAlgorithm encryptionAlgorithm = EncryptionAlgorithm.parseCode(in.readWord());
        int bitLength = in.readWord();
        Flags flags = Flags.parseCode(in.readWord());
        byte[] unknown = in.readBytes(4);

        return AlgIdExtraFieldRecord.builder()
                                    .dataSize(size)
                                    .format(format)
                                    .encryptionAlgorithm(encryptionAlgorithm)
                                    .bitLength(bitLength)
                                    .unknown(unknown)
                                    .flags(flags).build();
    }

}
