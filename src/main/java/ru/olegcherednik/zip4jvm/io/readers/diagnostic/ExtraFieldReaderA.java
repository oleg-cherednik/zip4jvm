package ru.olegcherednik.zip4jvm.io.readers.diagnostic;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.io.readers.AesExtraDataRecordReader;
import ru.olegcherednik.zip4jvm.io.readers.NtfsTimestampExtraFieldReader;
import ru.olegcherednik.zip4jvm.io.readers.Zip64Reader;
import ru.olegcherednik.zip4jvm.model.AesExtraDataRecord;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.ExtraField;
import ru.olegcherednik.zip4jvm.model.LocalFileHeader;
import ru.olegcherednik.zip4jvm.model.NtfsTimestampExtraField;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.utils.function.Reader;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static ru.olegcherednik.zip4jvm.model.builders.LocalFileHeaderBuilder.LOOK_IN_EXTRA_FIELD;

/**
 * @author Oleg Cherednik
 * @since 14.04.2019
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ExtraFieldReaderA implements Reader<ExtraField> {

    private final int size;
    private final ExtraFieldRecordReaderA extraFieldRecordReader;

    public ExtraFieldReaderA(int size, Map<Integer, Function<Integer, Reader<? extends ExtraField.Record>>> readers) {
        this(size, new ExtraFieldRecordReaderA(readers));
    }

    public static Map<Integer, Function<Integer, Reader<? extends ExtraField.Record>>> getReaders(CentralDirectory.FileHeader fileHeader) {
        boolean uncompressedSize = fileHeader.getUncompressedSize() == LOOK_IN_EXTRA_FIELD;
        boolean compressedSize = fileHeader.getCompressedSize() == LOOK_IN_EXTRA_FIELD;
        boolean offs = fileHeader.getLocalFileHeaderOffs() == LOOK_IN_EXTRA_FIELD;
        boolean disk = fileHeader.getDisk() == ZipModel.MAX_TOTAL_DISKS;
        return getReaders(uncompressedSize, compressedSize, offs, disk);
    }

    public static Map<Integer, Function<Integer, Reader<? extends ExtraField.Record>>> getReaders(LocalFileHeader localFileHeader) {
        boolean uncompressedSize = localFileHeader.getUncompressedSize() == LOOK_IN_EXTRA_FIELD;
        boolean compressedSize = localFileHeader.getCompressedSize() == LOOK_IN_EXTRA_FIELD;
        return getReaders(uncompressedSize, compressedSize, false, false);
    }

    private static Map<Integer, Function<Integer, Reader<? extends ExtraField.Record>>> getReaders(boolean uncompressedSize, boolean compressedSize,
            boolean offs, boolean disk) {
        Map<Integer, Function<Integer, Reader<? extends ExtraField.Record>>> map = new HashMap<>();

        if (uncompressedSize || compressedSize || offs || disk)
            map.put(Zip64.ExtendedInfo.SIGNATURE, size -> new Zip64Reader.ExtendedInfo(size, uncompressedSize, compressedSize, offs, disk));

        map.put(AesExtraDataRecord.SIGNATURE, AesExtraDataRecordReader::new);
        map.put(NtfsTimestampExtraField.SIGNATURE, NtfsTimestampExtraFieldReader::new);
        return map;
    }

    @Override
    public ExtraField read(DataInput in) throws IOException {
        return size > 0 ? readExtraField(in) : ExtraField.NULL;
    }

    protected ExtraField readExtraField(DataInput in) throws IOException {
        ExtraField.Builder builder = ExtraField.builder();
        long offsMax = in.getOffs() + size;

        while (in.getOffs() < offsMax)
            builder.addRecord(extraFieldRecordReader.read(in));

        return builder.build();
    }

}
