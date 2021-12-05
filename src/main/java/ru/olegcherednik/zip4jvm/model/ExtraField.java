/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package ru.olegcherednik.zip4jvm.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import ru.olegcherednik.zip4jvm.io.out.data.DataOutput;
import ru.olegcherednik.zip4jvm.model.extrafield.AesExtraFieldRecord;
import ru.olegcherednik.zip4jvm.model.extrafield.AlgIdExtraFieldRecord;
import ru.olegcherednik.zip4jvm.utils.function.Writer;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
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

    public AesExtraFieldRecord getAesRecord() {
        ExtraField.Record record = map.get(AesExtraFieldRecord.SIGNATURE);
        return record instanceof AesExtraFieldRecord ? (AesExtraFieldRecord)record : AesExtraFieldRecord.NULL;
    }

    public AlgIdExtraFieldRecord getAlgIdRecord() {
        ExtraField.Record record = map.get(AlgIdExtraFieldRecord.SIGNATURE);
        return record instanceof AlgIdExtraFieldRecord ? (AlgIdExtraFieldRecord)record : AlgIdExtraFieldRecord.NULL;
    }

    public Set<Integer> getSignatures() {
        return map.isEmpty() ? Collections.emptySet() : Collections.unmodifiableSet(map.keySet());
    }

    public Collection<Record> getRecords() {
        return map.isEmpty() ? Collections.emptyList() : Collections.unmodifiableCollection(map.values());
    }

    public ExtraField.Record getRecord(int signature) {
        return map.get(signature);
    }

    public int getTotalRecords() {
        return (int)map.values().stream()
                       .filter(record -> !record.isNull())
                       .count();
    }

    public int getSize() {
        return map.values().stream()
                  .mapToInt(Record::getBlockSize)
                  .sum();
    }

    @Override
    public String toString() {
        return this == NULL ? "<null>" : ("total: " + getTotalRecords());
    }

    public interface Record extends Writer {

        int getSignature();

        int getBlockSize();

        boolean isNull();

        String getTitle();

        @lombok.Builder
        @SuppressWarnings("InnerClassOfInterface")
        final class Unknown implements ExtraField.Record {

            @Getter
            private final int signature;
            private final byte[] data;

            public byte[] getData() {
                return ArrayUtils.clone(data);
            }

            @Override
            public int getBlockSize() {
                return data.length;
            }

            @Override
            public boolean isNull() {
                return false;
            }

            @Override
            public String getTitle() {
                return "Unknown";
            }

            @Override
            public void write(DataOutput out) throws IOException {
                out.writeWordSignature(signature);
                out.writeWord(data.length);
                out.write(data, 0, data.length);
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
