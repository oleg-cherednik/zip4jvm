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

import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.extrafield.PkwareExtraField;
import ru.olegcherednik.zip4jvm.view.BaseView;
import ru.olegcherednik.zip4jvm.view.PrintStreamDecorator;

import java.util.regex.Pattern;

/**
 * @param <T> {@link PkwareExtraField.Record}
 * @author Oleg Cherednik
 * @since 24.11.2019
 */
public abstract class ExtraFieldRecordView<T extends PkwareExtraField.Record> extends BaseView {

    private static final Pattern SPACE = Pattern.compile(" ");
    private static final Pattern SLASH = Pattern.compile("[/\\\\]");

    protected final T record;
    protected final Block block;

    protected ExtraFieldRecordView(int offs, int columnWidth, long totalDisks,
                                   T record, Block block) {
        super(offs, columnWidth, totalDisks);
        this.record = requireNotNull(record);
        this.block = block;
    }

    protected abstract void printRecord(PrintStreamDecorator out);

    // ---------- View ----------

    @Override
    public boolean printTextInfo(PrintStreamDecorator out) {
        printTitle(out);
        printRecord(out);
        return true;
    }

    // ----------

    private void printTitle(PrintStreamDecorator out) {
        printValueWithLocation(out, String.format("(0x%04X) %s:", getSignature(), getTitle()), block);
    }

    protected int getSignature() {
        return record.getSignature();
    }

    protected String getTitle() {
        return record.getTitle();
    }

    public String getFileName() {
        String title = getTitle();
        title = SPACE.matcher(title).replaceAll("_");
        title = SLASH.matcher(title).replaceAll("-");
        return String.format("(0x%04X)_%s", getSignature(), title);
    }

    // ---------- static ----------

    private static <T extends PkwareExtraField.Record> T requireNotNull(T record) {
        if (record == null || record.isNull())
            throw new IllegalArgumentException("Parameter should not be null: record");

        return record;
    }

}
