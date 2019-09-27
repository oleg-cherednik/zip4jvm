package ru.olegcherednik.zip4jvm.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
        map = builder.map.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(builder.map);
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

    public interface Record {

        int getSignature();

        int getBlockSize();

        boolean isNull();

        @Getter
        @RequiredArgsConstructor
        @SuppressWarnings("InnerClassOfInterface")
        final class Unknown implements ExtraField.Record {

            private final int signature;
            private final byte[] data;

            @Override
            public int getBlockSize() {
                return data.length;
            }

            @Override
            public boolean isNull() {
                return false;
            }
        }

    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Builder {

        private final Map<Integer, Record> map = new HashMap<>();

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
