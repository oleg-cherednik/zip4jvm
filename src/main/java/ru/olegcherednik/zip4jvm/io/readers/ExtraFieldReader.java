package ru.olegcherednik.zip4jvm.io.readers;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.ArrayUtils;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.model.AesExtraDataRecord;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.ExtraField;
import ru.olegcherednik.zip4jvm.model.LocalFileHeader;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.utils.function.Reader;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static ru.olegcherednik.zip4jvm.model.builders.LocalFileHeaderBuilder.LOOK_IN_EXTRA_FIELD;

/**
 * @author Oleg Cherednik
 * @since 14.04.2019
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
final class ExtraFieldReader implements Reader<ExtraField> {

    private final int size;
    private final Map<Integer, Reader<? extends ExtraField.Record>> map;

    public static ExtraFieldReader build(int size, CentralDirectory.FileHeader fileHeader) {
        boolean uncompressedSize = fileHeader.getUncompressedSize() == LOOK_IN_EXTRA_FIELD;
        boolean compressedSize = fileHeader.getCompressedSize() == LOOK_IN_EXTRA_FIELD;
        boolean offs = fileHeader.getOffsLocalFileHeader() == LOOK_IN_EXTRA_FIELD;
        boolean disk = fileHeader.getDisk() == ZipModel.MAX_TOTAL_DISKS;

        Map<Integer, Reader<? extends ExtraField.Record>> map = new HashMap<>();
        map.put(Zip64.ExtendedInfo.SIGNATURE, createZip64ExtendedInfoReader(uncompressedSize, compressedSize, offs, disk));
        map.put(AesExtraDataRecord.SIGNATURE, new AesExtraDataRecordReader(AesExtraDataRecord.SIGNATURE));

        return new ExtraFieldReader(size, map);
    }

    public static ExtraFieldReader build(int size, LocalFileHeader localFileHeader) {
        boolean uncompressedSize = localFileHeader.getUncompressedSize() == LOOK_IN_EXTRA_FIELD;
        boolean compressedSize = localFileHeader.getCompressedSize() == LOOK_IN_EXTRA_FIELD;

        Map<Integer, Reader<? extends ExtraField.Record>> map = new HashMap<>();
        map.put(Zip64.ExtendedInfo.SIGNATURE, createZip64ExtendedInfoReader(uncompressedSize, compressedSize, false, false));
        map.put(AesExtraDataRecord.SIGNATURE, new AesExtraDataRecordReader(AesExtraDataRecord.SIGNATURE));

        return new ExtraFieldReader(size, map);
    }

    @SuppressWarnings("NewMethodNamingConvention")
    private static Reader<? extends ExtraField.Record> createZip64ExtendedInfoReader(boolean uncompressedSize, boolean compressedSize, boolean offs,
            boolean disk) {
        if (uncompressedSize || compressedSize || offs || disk)
            return new Zip64Reader.ExtendedInfo(uncompressedSize, compressedSize, offs, disk);
        return new UnknownExtraFileRecordReader(Zip64.ExtendedInfo.SIGNATURE);
    }

    @Override
    public ExtraField read(DataInput in) throws IOException {
        ExtraField.Builder builder = ExtraField.builder();

        if (size <= 0)
            return builder.build();

        final long offsMax = in.getOffs() + size;

        while (in.getOffs() < offsMax) {
            int signature = in.readWord();
            builder.addRecord(getReader(signature).read(in));
        }

        return builder.build();
    }

    private Reader<? extends ExtraField.Record> getReader(int signature) {
        return map.containsKey(signature) ? map.get(signature) : new UnknownExtraFileRecordReader(signature);
    }

    @RequiredArgsConstructor
    private static final class UnknownExtraFileRecordReader implements Reader<ExtraField.Record.Unknown> {

        private final int signature;

        @Override
        public ExtraField.Record.Unknown read(DataInput in) throws IOException {
            int size = in.readWord();
            byte[] data = in.readBytes(size);
            return new ExtraField.Record.Unknown(signature, data == null ? ArrayUtils.EMPTY_BYTE_ARRAY : data);
        }
    }

}
