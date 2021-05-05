package ru.olegcherednik.zip4jvm.model.extrafield;

import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.NotImplementedException;
import ru.olegcherednik.zip4jvm.crypto.strong.EncryptionAlgorithm;
import ru.olegcherednik.zip4jvm.crypto.strong.Flags;
import ru.olegcherednik.zip4jvm.io.out.data.DataOutput;
import ru.olegcherednik.zip4jvm.model.ExtraField;

import java.io.IOException;

/**
 * see 7.2.3
 *
 * @author Oleg Cherednik
 * @since 15.02.2020
 */
@Getter
@Builder
public final class AlgIdExtraFieldRecord implements ExtraField.Record {

    public static final AlgIdExtraFieldRecord NULL = builder().encryptionAlgorithm(EncryptionAlgorithm.UNKNOWN).build();

    public static final int SIGNATURE = 0x0017;
    public static final int SIZE_FIELD = 2 + 2; // 4 bytes: signature + size

    // size:2 - tag for this "extra" block type (0x0017)
    // size:2 - size of total "extra" block
    private final int dataSize;
    // size:2 - format definition for this record (should be 2)
    private final int format;
    // size:2 - encryption algorithm identifier
    private final EncryptionAlgorithm encryptionAlgorithm;
    // size:2 - bit length of encryption key
    private final int bitLength;
    // size:2 - processing flags
    private final Flags flags;
    private final byte[] unknown;

    @Override
    public int getSignature() {
        return SIGNATURE;
    }

    @Override
    public int getBlockSize() {
        return this == NULL ? 0 : dataSize + SIZE_FIELD;
    }

    @Override
    public boolean isNull() {
        return this == NULL;
    }

    @Override
    public String getTitle() {
        return "AlgId";
    }

    @Override
    public void write(DataOutput out) throws IOException {
        throw new NotImplementedException();
    }

}
