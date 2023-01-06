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
package ru.olegcherednik.zip4jvm.view.extrafield;

import lombok.Getter;
import org.apache.commons.lang3.ArrayUtils;
import ru.olegcherednik.zip4jvm.model.GeneralPurposeFlag;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.extrafield.PkwareExtraField;

import java.util.Objects;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 03.12.2019
 */
@Getter
final class Builder<R extends PkwareExtraField.Record, V extends ExtraFieldRecordView<R>> {

    private final Function<Builder<R, V>, V> sup;
    private R record;
    private GeneralPurposeFlag generalPurposeFlag;
    private byte[] data = ArrayUtils.EMPTY_BYTE_ARRAY;
    private Block block;
    private int offs;
    private int columnWidth;
    private long totalDisks;

    Builder(Function<Builder<R, V>, V> sup) {
        this.sup = sup;
    }

    public V build() {
        check();
        return sup.apply(this);
    }

    public void check() {
        Objects.requireNonNull(data, "'data' must not be null");
        Objects.requireNonNull(block, "'block' must not be null");
    }

    public Builder<R, V> record(R record) {
        this.record = record == null || record.isNull() ? null : record;
        return this;
    }

    public Builder<R, V> generalPurposeFlag(GeneralPurposeFlag generalPurposeFlag) {
        this.generalPurposeFlag = generalPurposeFlag;
        return this;
    }

    @SuppressWarnings("MethodCanBeVariableArityMethod")
    public Builder<R, V> data(byte[] data) {
        this.data = ArrayUtils.isEmpty(data) ? ArrayUtils.EMPTY_BYTE_ARRAY : ArrayUtils.clone(data);
        return this;
    }

    public Builder<R, V> block(Block block) {
        this.block = block == Block.NULL ? null : block;
        return this;
    }

    public Builder<R, V> position(int offs, int columnWidth, long totalDisks) {
        this.offs = offs;
        this.columnWidth = columnWidth;
        this.totalDisks = totalDisks;
        return this;
    }

}
