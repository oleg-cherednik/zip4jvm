package ru.olegcherednik.zip4jvm.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.ArrayUtils;
import ru.olegcherednik.zip4jvm.io.out.DataOutput;
import ru.olegcherednik.zip4jvm.utils.function.Writer;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Oleg Cherednik
 * @since 14.04.2019
 */
public final class ExtraField {

    public static final ExtraField NULL = new ExtraField();

    public static final int NO_DATA = -1;

    private final Map<Integer, ExtraField.Record> map;

    public static Builder builder() {
        return new Builder();
    }

    private ExtraField() {
        map = Collections.emptyMap();
    }

    private ExtraField(Builder builder) {
        map = Collections.unmodifiableMap(builder.map);
    }

    public Zip64.ExtendedInfo getExtendedInfo() {
        ExtraField.Record record = map.get(Zip64.ExtendedInfo.SIGNATURE);
        return record instanceof Zip64.ExtendedInfo ? (Zip64.ExtendedInfo)record : Zip64.ExtendedInfo.NULL;
    }

    public AesExtraDataRecord getAesExtraDataRecord() {
        ExtraField.Record record = map.get(AesExtraDataRecord.SIGNATURE);
        return record instanceof AesExtraDataRecord ? (AesExtraDataRecord)record : AesExtraDataRecord.NULL;
    }

    public Collection<Record> getRecords() {
        return map.isEmpty() ? Collections.emptyList() : Collections.unmodifiableCollection(map.values());
    }

    public int getSize() {
        return map.values().stream()
                  .mapToInt(Record::getBlockSize)
                  .sum();
    }

    @Override
    public String toString() {
        return this == NULL ? "<null>" : ("total: " + map.size());
    }

    public interface Record extends Writer {

        int getSignature();

        int getBlockSize();

        boolean isNull();

        @RequiredArgsConstructor
        @SuppressWarnings("InnerClassOfInterface")
        final class Unknown implements ExtraField.Record {

            @Getter
            private final int signature;
            private final byte[] blockData;

            public byte[] getBlockData() {
                return ArrayUtils.clone(blockData);
            }

            @Override
            public int getBlockSize() {
                return blockData.length;
            }

            @Override
            public boolean isNull() {
                return false;
            }

            @Override
            public void write(DataOutput out) throws IOException {
                out.writeWordSignature(signature);
                out.writeWord(blockData.length);
                out.write(blockData, 0, blockData.length);
            }
        }

    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Builder {

        private final Map<Integer, Record> map = new TreeMap<>();

        public ExtraField build() {
            return map.isEmpty() ? NULL : new ExtraField(this);
        }

        public Builder addRecord(Record record) {
            if (record != null && !record.isNull())
                map.put(record.getSignature(), record);
            return this;
        }

        public Builder addRecord(ExtraField extraField) {
            map.putAll(extraField.map);
            return this;
        }

    }

}
