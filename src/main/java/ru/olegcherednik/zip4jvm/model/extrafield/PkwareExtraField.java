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
package ru.olegcherednik.zip4jvm.model.extrafield;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.out.data.DataOutput;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.extrafield.records.AesExtraFieldRecord;
import ru.olegcherednik.zip4jvm.model.extrafield.records.StrongEncryptionHeaderExtraFieldRecord;
import ru.olegcherednik.zip4jvm.utils.function.Writer;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author Oleg Cherednik
 * @since 14.04.2019
 */
public class PkwareExtraField implements ExtraField {

    public static final PkwareExtraField NULL = new PkwareExtraField();

    public static final int NO_DATA = -1;

    private final Map<Integer, Record> map;

    public PkwareExtraField(List<Record> records) {
        map = new TreeMap<>();

        records.stream()
               .filter(record -> !record.isNull())
               .forEach(record -> {
                   if (map.put(record.getSignature(), record) != null)
                       throw new Zip4jvmException("Records signature should be unique");
               });
    }

    public static Builder builder() {
        return new Builder();
    }

    protected PkwareExtraField() {
        map = Collections.emptyMap();
    }

    private PkwareExtraField(Builder builder) {
        map = Collections.unmodifiableMap(builder.map);
    }

    public Zip64.ExtendedInfo getExtendedInfo() {
        PkwareExtraField.Record record = map.get(Zip64.ExtendedInfo.SIGNATURE);
        return record instanceof Zip64.ExtendedInfo ? (Zip64.ExtendedInfo)record : Zip64.ExtendedInfo.NULL;
    }

    public AesExtraFieldRecord getAesRecord() {
        PkwareExtraField.Record record = map.get(AesExtraFieldRecord.SIGNATURE);
        return record instanceof AesExtraFieldRecord ? (AesExtraFieldRecord)record : AesExtraFieldRecord.NULL;
    }

    public StrongEncryptionHeaderExtraFieldRecord getStrongEncryptionHeaderRecord() {
        PkwareExtraField.Record record = map.get(StrongEncryptionHeaderExtraFieldRecord.SIGNATURE);

        if (record instanceof StrongEncryptionHeaderExtraFieldRecord)
            return (StrongEncryptionHeaderExtraFieldRecord)record;

        return StrongEncryptionHeaderExtraFieldRecord.NULL;
    }

    public Set<Integer> getSignatures() {
        return map.isEmpty() ? Collections.emptySet() : Collections.unmodifiableSet(map.keySet());
    }

    public Collection<Record> getRecords() {
        return map.isEmpty() ? Collections.emptyList() : Collections.unmodifiableCollection(map.values());
    }

    public PkwareExtraField.Record getRecord(int signature) {
        return map.get(signature);
    }

    public int getTotalRecords() {
        return (int)map.values().stream()
                       .filter(record -> !record.isNull())
                       .count();
    }

    @Override
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

    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Builder {

        private final Map<Integer, Record> map = new TreeMap<>();

        public PkwareExtraField build() {
            return map.isEmpty() ? NULL : new PkwareExtraField(this);
        }

        public Builder addRecord(Record record) {
            if (record != null && !record.isNull())
                map.put(record.getSignature(), record);
            return this;
        }

        public Builder addRecord(PkwareExtraField extraField) {
            map.putAll(extraField.map);
            return this;
        }

    }

}
