package ru.olegcherednik.zip4jvm.io.readers;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.model.NtfsTimestampExtraField;
import ru.olegcherednik.zip4jvm.utils.function.Reader;
import ru.olegcherednik.zip4jvm.utils.time.NtfsTimestampConverter;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 16.10.2019
 */
@RequiredArgsConstructor
public final class NtfsTimestampExtraFieldReader implements Reader<NtfsTimestampExtraField> {

    private final int size;

    @Override
    public NtfsTimestampExtraField read(DataInput in) throws IOException {
        long offs = in.getOffs();

        in.skip(4);

        NtfsTimestampExtraField extraField = NtfsTimestampExtraField.NULL;

        while (in.getOffs() < offs + size) {
            int tag = in.readWord();

            if (tag == NtfsTimestampExtraField.TAG_ONE && extraField == NtfsTimestampExtraField.NULL)
                extraField = readTagOne(in);
            else
                in.skip(in.readWord());
        }

        return extraField;
    }

    private static NtfsTimestampExtraField readTagOne(DataInput in) throws IOException {
        int size = in.readWord();
        long lastModificationTime = NtfsTimestampConverter.ntfsToJavaTime(in.readQword());
        long lastAccessTime = NtfsTimestampConverter.ntfsToJavaTime(in.readQword());
        long creationTime = NtfsTimestampConverter.ntfsToJavaTime(in.readQword());

        return NtfsTimestampExtraField.builder()
                                      .dataSize(size)
                                      .lastModificationTime(lastModificationTime)
                                      .lastAccessTime(lastAccessTime)
                                      .creationTime(creationTime).build();
    }

    @Override
    public String toString() {
        return String.format("NTFS Timestamps (0x%04X)", NtfsTimestampExtraField.SIGNATURE);
    }

}
