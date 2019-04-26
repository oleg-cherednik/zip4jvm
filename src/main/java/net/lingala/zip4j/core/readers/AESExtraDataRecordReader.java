package net.lingala.zip4j.core.readers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.model.AESExtraDataRecord;
import net.lingala.zip4j.model.AESStrength;
import net.lingala.zip4j.model.CompressionMethod;
import net.lingala.zip4j.util.LittleEndianRandomAccessFile;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 14.04.2019
 */
@RequiredArgsConstructor
final class AESExtraDataRecordReader {

    private final short signature;

    @NonNull
    public AESExtraDataRecord read(@NonNull LittleEndianRandomAccessFile in) throws IOException {
        if (signature != AESExtraDataRecord.SIGNATURE)
            return AESExtraDataRecord.NULL;

        AESExtraDataRecord record = new AESExtraDataRecord();
        record.setDataSize(in.readWord());
        record.setVersionNumber(in.readShort());
        record.setVendor(in.readString(2));
        record.setAesStrength(AESStrength.parseByte(in.readByte()));
        record.setCompressionMethod(CompressionMethod.parseValue(in.readShort()));
        return record;
    }
}
