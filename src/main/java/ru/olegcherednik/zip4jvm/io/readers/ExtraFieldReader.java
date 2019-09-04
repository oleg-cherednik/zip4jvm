package ru.olegcherednik.zip4jvm.io.readers;

import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.model.ExtraField;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.AesExtraDataRecord;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 14.04.2019
 */
@RequiredArgsConstructor
final class ExtraFieldReader {

    private final int size;
    private final boolean uncompressedSize;
    private final boolean compressedSize;
    private final boolean offs;
    private final boolean disk;

    public ExtraField read(@NonNull DataInput in) throws IOException {
        ExtraField extraField = new ExtraField();

        if (size <= 0)
            return extraField;

        final long offsMax = in.getOffs() + size;

        while (in.getOffs() < offsMax) {
            int signature = in.readWord();

            Zip64.ExtendedInfo zip64 = new Zip64Reader.ExtendedInfo(signature, uncompressedSize, compressedSize, offs, disk).read(in);
            AesExtraDataRecord aes = new AesExtraDataRecordReader(signature).read(in);

            if (zip64 != Zip64.ExtendedInfo.NULL)
                extraField.setExtendedInfo(zip64);
            else if (aes != AesExtraDataRecord.NULL)
                extraField.setAesExtraDataRecord(aes);
            else
                in.skip(in.readWord());
        }

        return extraField;
    }

}
