package ru.olegcherednik.zip4jvm.io.readers;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.model.NTFSTimestampExtraField;
import ru.olegcherednik.zip4jvm.utils.function.Reader;
import ru.olegcherednik.zip4jvm.utils.time.NtfsTimestampConverter;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 16.10.2019
 */
@RequiredArgsConstructor
@SuppressWarnings("NewClassNamingConvention")
final class NTFSTimestampExtraFieldReader implements Reader<NTFSTimestampExtraField> {

    private final int size;

    @Override
    public NTFSTimestampExtraField read(DataInput in) throws IOException {
        long offs = in.getOffs();

        in.skip(4);

        NTFSTimestampExtraField extraField = NTFSTimestampExtraField.NULL;

        while (in.getOffs() < offs + size) {
            int tag = in.readWord();

            if (tag == NTFSTimestampExtraField.TAG_ONE && extraField == NTFSTimestampExtraField.NULL)
                extraField = readTagOne(in);
            else
                in.skip(in.readWord());
        }

        return extraField;
    }

    private static NTFSTimestampExtraField readTagOne(DataInput in) throws IOException {
        int size = in.readWord();
        long mtime = NtfsTimestampConverter.ntfsToJavaTime(in.readQword());
        long atime = NtfsTimestampConverter.ntfsToJavaTime(in.readQword());
        long ctime = NtfsTimestampConverter.ntfsToJavaTime(in.readQword());

        return NTFSTimestampExtraField.builder()
                                      .dataSize(size)
                                      .creationDate(ctime)
                                      .lastModifiedDate(mtime)
                                      .lastAccessedDate(atime).build();
    }

    @Override
    public String toString() {
        return String.format("NTFS Timestamps (0x%04X)", NTFSTimestampExtraField.SIGNATURE);
    }

}
