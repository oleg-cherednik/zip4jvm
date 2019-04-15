package net.lingala.zip4j.core.readers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.model.AESExtraDataRecord;
import net.lingala.zip4j.model.CentralDirectory;
import net.lingala.zip4j.model.ExtraDataRecord;
import net.lingala.zip4j.model.Zip64ExtendedInfo;
import net.lingala.zip4j.util.LittleEndianRandomAccessFile;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Oleg Cherednik
 * @since 14.04.2019
 */
@RequiredArgsConstructor
final class ExtraFieldReader {

    private final int size;

    public Map<Short, ExtraDataRecord> read(@NonNull LittleEndianRandomAccessFile in) throws IOException {
        if (size <= 0)
            return Collections.emptyMap();

        final long offsMax = in.getFilePointer() + size;
        Map<Short, ExtraDataRecord> map = new HashMap<>();

        while (in.getFilePointer() < offsMax) {
            ExtraDataRecord record = new ExtraDataRecord();
            record.setHeader(in.readShort());
            record.setSizeOfData(in.readShort());

            if (record.getSizeOfData() == 0)
                continue;
            if (in.getFilePointer() + record.getSizeOfData() > offsMax)
                // extra data record is corrupt; skip reading any further extra data
                break;

            record.setData(in.readBytes(record.getSizeOfData()));
            map.put(record.getHeader(), record);
        }

        return map.isEmpty() ? Collections.emptyMap() : map;
    }

    public void read(@NonNull LittleEndianRandomAccessFile in, @NonNull CentralDirectory.FileHeader fileHeader) throws IOException {
        if (size <= 0)
            return;

        Zip64ExtendedInfo zip64;
        AESExtraDataRecord aes;
        final long offsMax = in.getFilePointer() + size;

        while (in.getFilePointer() < offsMax) {
            short signature = in.readWord();

            if ((zip64 = new Zip64ExtendedInfoReader(signature, fileHeader).read(in)) != null)
                fileHeader.setZip64ExtendedInfo(zip64);
            else if ((aes = new AESExtraDataRecordReader(signature).read(in)) != null)
                fileHeader.setAesExtraDataRecord(aes);
            else
                // TODO do add skip instead
                in.readBytes(in.readWord());
        }
    }

}
