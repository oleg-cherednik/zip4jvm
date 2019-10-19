package ru.olegcherednik.zip4jvm.io.readers;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.ArrayUtils;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.model.AesExtraDataRecord;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.ExtraField;
import ru.olegcherednik.zip4jvm.model.LocalFileHeader;
import ru.olegcherednik.zip4jvm.model.NtfsTimestampExtraField;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.diagnostic.Block;
import ru.olegcherednik.zip4jvm.model.diagnostic.Diagnostic;
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
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
final class ExtraFieldReader implements Reader<ExtraField> {

    private static final String MARK_EXTRA_FIELD_RECORD_OFFS = "extraFieldRecordOffs";

    private final int size;
    private final Map<Integer, Function<Integer, Reader<? extends ExtraField.Record>>> map;

    public static ExtraFieldReader build(int size, CentralDirectory.FileHeader fileHeader) {
        boolean uncompressedSize = fileHeader.getUncompressedSize() == LOOK_IN_EXTRA_FIELD;
        boolean compressedSize = fileHeader.getCompressedSize() == LOOK_IN_EXTRA_FIELD;
        boolean offs = fileHeader.getLocalFileHeaderOffs() == LOOK_IN_EXTRA_FIELD;
        boolean disk = fileHeader.getDisk() == ZipModel.MAX_TOTAL_DISKS;

        Map<Integer, Function<Integer, Reader<? extends ExtraField.Record>>> map = getReaders(uncompressedSize, compressedSize, offs, disk);
        return new ExtraFieldReader(size, map);
    }

    public static ExtraFieldReader build(int size, LocalFileHeader localFileHeader) {
        boolean uncompressedSize = localFileHeader.getUncompressedSize() == LOOK_IN_EXTRA_FIELD;
        boolean compressedSize = localFileHeader.getCompressedSize() == LOOK_IN_EXTRA_FIELD;

        Map<Integer, Function<Integer, Reader<? extends ExtraField.Record>>> map = getReaders(uncompressedSize, compressedSize, false, false);
        return new ExtraFieldReader(size, map);
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
        ExtraField.Builder builder = ExtraField.builder();

        if (size <= 0)
            return builder.build();

        Diagnostic.getInstance().getCentralDirectory().getFileHeader().addExtraField();
        Diagnostic.getInstance().getCentralDirectory().getFileHeader().getExtraField().setOffs(in.getOffs());

        final long offsMax = in.getOffs() + size;

        while (in.getOffs() < offsMax) {
            in.mark(MARK_EXTRA_FIELD_RECORD_OFFS);

            Diagnostic.getInstance().getCentralDirectory().getFileHeader().getExtraField().addRecord();
            Block block = Diagnostic.getInstance().getCentralDirectory().getFileHeader().getExtraField().getRecord();

            ExtraField.Record record = Block.foo(in, block, () -> {
                int signature = in.readWord();
                int size = in.readWord();
                ExtraField.Record r = getRecord(signature, size, in);
                builder.addRecord(r);
                return r;
            });
            Diagnostic.getInstance().getCentralDirectory().getFileHeader().getExtraField().saveRecord(record.getSignature());
        }

        Diagnostic.getInstance().getCentralDirectory().getFileHeader().getExtraField().setEndOffs(in.getOffs());

        return builder.build();
    }


    private ExtraField.Record getRecord(int signature, int size, DataInput in) throws IOException {
        if (map.containsKey(signature))
            return map.get(signature).apply(size).read(in);

        long offs = in.getMark(MARK_EXTRA_FIELD_RECORD_OFFS);

        int signatureAndSizeLength = (int)(in.getOffs() - offs);
        in.seek(offs);
        byte[] data = in.readBytes(signatureAndSizeLength + size);
        return new ExtraField.Record.Unknown(signature, data == null ? ArrayUtils.EMPTY_BYTE_ARRAY : data);
    }

}
