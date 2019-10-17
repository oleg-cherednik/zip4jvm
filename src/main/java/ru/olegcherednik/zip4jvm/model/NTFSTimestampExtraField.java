package ru.olegcherednik.zip4jvm.model;

import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang.NotImplementedException;
import ru.olegcherednik.zip4jvm.io.out.DataOutput;

import java.io.IOException;

/**
 * see 4.5.5
 *
 * @author Oleg Cherednik
 * @since 16.10.2019
 */
@Getter
@Builder
@SuppressWarnings("NewClassNamingConvention")
public final class NTFSTimestampExtraField implements ExtraField.Record {

    public static final NTFSTimestampExtraField NULL = builder().build();

    public static final int SIGNATURE = 0x0000A;
    public static final int SIZE_FIELD = 2 + 2; // 4 bytes: signature + size
    public static final int TAG_ONE = 0x0001;

    private final int dataSize;
    // TAG_ONE
    private final long creationDate;
    private final long lastModifiedDate;
    private final long lastAccessedDate;

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
    public void write(DataOutput out) throws IOException {
        throw new NotImplementedException();
    }

}
