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

import ru.olegcherednik.zip4jvm.model.ExtraField;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.view.BaseView;

import java.io.PrintStream;
import java.util.regex.Pattern;

/**
 * @author Oleg Cherednik
 * @since 24.11.2019
 */
public abstract class ExtraFieldRecordView<T extends ExtraField.Record> extends BaseView {

    private static final Pattern SPACE = Pattern.compile(" ");
    private static final Pattern SLASH = Pattern.compile("[/\\\\]");

    private final T record;
    private final Block block;
    private final PrintConsumer<T, BaseView> printConsumer;

    protected ExtraFieldRecordView(Builder<T, ?> builder, PrintConsumer<T, BaseView> printConsumer) {
        super(builder.getOffs(), builder.getColumnWidth(), builder.getTotalDisks());
        record = builder.getRecord();
        block = builder.getBlock();
        this.printConsumer = printConsumer;
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

    @Override
    public boolean print(PrintStream out) {
        if (record == null)
            return false;

        printValueWithLocation(out, String.format("(0x%04X) %s:", getSignature(), getTitle()), block);
        printConsumer.print(record, this, out);

        return true;
    }

}
