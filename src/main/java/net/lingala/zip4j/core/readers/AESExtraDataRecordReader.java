package net.lingala.zip4j.core.readers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.model.AESExtraDataRecord;
import net.lingala.zip4j.model.AESStrength;
import net.lingala.zip4j.model.CompressionMethod;
import net.lingala.zip4j.util.InternalZipConstants;
import net.lingala.zip4j.util.LittleEndianRandomAccessFile;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 14.04.2019
 */
@RequiredArgsConstructor
final class AESExtraDataRecordReader {

    private final short header;
    private final short size;

    public AESExtraDataRecord read(@NonNull LittleEndianRandomAccessFile in) throws IOException {
        if (header != InternalZipConstants.AESSIG)
            return null;

        AESExtraDataRecord res = new AESExtraDataRecord();
        res.setDataSize(size);
        res.setVersionNumber(in.readShort());
        res.setVendor(in.readString(2));
        res.setAesStrength(AESStrength.parseByte(in.readByte()));
        res.setCompressionMethod(CompressionMethod.parseValue(in.readShort()));
        return res;
    }
}
