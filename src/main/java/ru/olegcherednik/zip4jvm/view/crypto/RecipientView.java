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
package ru.olegcherednik.zip4jvm.view.crypto;

import ru.olegcherednik.zip4jvm.crypto.strong.Recipient;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.view.BaseView;
import ru.olegcherednik.zip4jvm.view.ByteArrayHexView;
import ru.olegcherednik.zip4jvm.view.SizeView;

import java.io.PrintStream;
import java.util.Objects;

/**
 * @author Oleg Cherednik
 * @since 30.03.2020
 */
@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
public final class RecipientView extends BaseView {

    private final int num;
    private final Recipient recipient;
    private final Block block;

    public static Builder builder() {
        return new Builder();
    }

    private RecipientView(Builder builder) {
        super(builder.offs, builder.columnWidth, builder.totalDisks);
        num = builder.num;
        recipient = builder.recipient;
        block = builder.block;
    }

    @Override
    public boolean printTextInfo(PrintStream out) {
        printValueWithLocation(out, String.format("#%d - location:", num + 1), block);
        printLine(out, "  size:", recipient.getSize());
        printHash(out);
        printSimpleKeyBlob(out);
        return true;
    }

    private void printHash(PrintStream out) {
        new SizeView("  public key hash:", recipient.getHash().length, offs, columnWidth).printTextInfo(out);
        new ByteArrayHexView(recipient.getHash(), offs, columnWidth).printTextInfo(out);
    }

    private void printSimpleKeyBlob(PrintStream out) {
        new SizeView("  simple key blob:", recipient.getSimpleKeyBlob().length, offs, columnWidth).printTextInfo(out);
        new ByteArrayHexView(recipient.getSimpleKeyBlob(), offs, columnWidth).printTextInfo(out);
    }

    public static final class Builder {

        private int num;
        private Recipient recipient;
        private Block block;
        private int offs;
        private int columnWidth;
        private long totalDisks;

        public RecipientView build() {
            Objects.requireNonNull(recipient, "'recipient' must not be null");
            Objects.requireNonNull(block, "'block' must not be null");
            return new RecipientView(this);
        }

        public Builder num(int num) {
            this.num = num;
            return this;
        }

        public Builder recipient(Recipient recipient) {
            this.recipient = recipient;
            return this;
        }

        public Builder block(Block block) {
            this.block = block;
            return this;
        }

        public Builder position(int offs, int columnWidth, long totalDisks) {
            this.offs = offs;
            this.columnWidth = columnWidth;
            this.totalDisks = totalDisks;
            return this;
        }
    }
}
