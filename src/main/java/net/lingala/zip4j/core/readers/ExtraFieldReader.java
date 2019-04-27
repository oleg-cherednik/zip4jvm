package net.lingala.zip4j.core.readers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.model.AESExtraDataRecord;
import net.lingala.zip4j.model.ExtraField;
import net.lingala.zip4j.model.Zip64;
import net.lingala.zip4j.io.LittleEndianRandomAccessFile;

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
    private final boolean diskNumber;

    public ExtraFieldReader(int size) {
        this(size, false, false, false, false);
    }

    @NonNull
    public ExtraField read(@NonNull LittleEndianRandomAccessFile in) throws IOException {
        if (size <= 0)
            return ExtraField.NULL;

        ExtraField extraField = new ExtraField();
        final long offsMax = in.getFilePointer() + size;

        while (in.getFilePointer() < offsMax) {
            short signature = in.readWord();

            Zip64.ExtendedInfo zip64 = new Zip64ExtendedInfoReader(signature, uncompressedSize, compressedSize, offs, diskNumber).read(in);
            AESExtraDataRecord aes = new AESExtraDataRecordReader(signature).read(in);

            if (zip64 != Zip64.ExtendedInfo.NULL)
                extraField.setExtendedInfo(zip64);
            else if (aes != AESExtraDataRecord.NULL)
                extraField.setAesExtraDataRecord(aes);
            else
                // TODO do add skip instead
                in.readBytes(in.readWord());
        }

        return extraField.isEmpty() ? ExtraField.NULL : extraField;
    }

}
