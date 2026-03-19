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
package ru.olegcherednik.zip4jvm.view.entry;

import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.GeneralPurposeFlag;
import ru.olegcherednik.zip4jvm.model.LocalFileHeader;
import ru.olegcherednik.zip4jvm.model.block.ZipEntryBlock;
import ru.olegcherednik.zip4jvm.model.charset.Charsets;
import ru.olegcherednik.zip4jvm.model.extrafield.AlignmentExtraField;
import ru.olegcherednik.zip4jvm.model.extrafield.ExtraField;
import ru.olegcherednik.zip4jvm.model.extrafield.PkwareExtraField;
import ru.olegcherednik.zip4jvm.view.BaseView;
import ru.olegcherednik.zip4jvm.view.ByteArrayHexView;
import ru.olegcherednik.zip4jvm.view.CompressionView;
import ru.olegcherednik.zip4jvm.view.GeneralPurposeFlagView;
import ru.olegcherednik.zip4jvm.view.LastModifiedTimeView;
import ru.olegcherednik.zip4jvm.view.SizeView;
import ru.olegcherednik.zip4jvm.view.StringHexView;
import ru.olegcherednik.zip4jvm.view.VersionView;
import ru.olegcherednik.zip4jvm.view.extrafield.ExtraFieldView;
import ru.olegcherednik.zip4jvm.view.out.Out;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.nio.charset.Charset;
import java.util.Optional;

/**
 * @author Oleg Cherednik
 * @since 23.10.2019
 */
public final class LocalFileHeaderView extends BaseView {

    private final LocalFileHeader localFileHeader;
    private final ZipEntryBlock.LocalFileHeaderBlock localFileHeaderBlock;
    private final long pos;
    private final Charset charset;
    private final boolean centralDirectoryEncrypted;

    public static Builder builder() {
        return new Builder();
    }

    private LocalFileHeaderView(Builder builder) {
        super(builder.offs, builder.columnWidth, builder.totalDisks);
        localFileHeader = builder.localFileHeader;
        localFileHeaderBlock = builder.diagLocalFileHeader;
        pos = builder.pos;
        charset = builder.charset;
        centralDirectoryEncrypted = builder.centralDirectoryEncrypted;
    }

    @Override
    public boolean printTextInfo(Out out) {
        printSubTitle(out, LocalFileHeader.SIGNATURE, pos, '[' + charset.name() + "] " + localFileHeader.getFileName(),
                      localFileHeaderBlock.getContent());
        printVersion(out);
        printGeneralPurposeFlag(out);
        printCompressionMethod(out);
        printLastModifiedTime(out);
        printCrc32(out);
        printSize(out);
        printFileName(out);
        printExtraField(out);

        return true;
    }

    private void printVersion(Out out) {
        new VersionView(null, localFileHeader.getVersionToExtract(), offs, columnWidth).printTextInfo(out);
    }

    private void printGeneralPurposeFlag(Out out) {
        new GeneralPurposeFlagView(localFileHeader.getGeneralPurposeFlag(),
                                   localFileHeader.getCompression(),
                                   offs,
                                   columnWidth).printTextInfo(out);
    }

    private void printCompressionMethod(Out out) {
        Compression compression = localFileHeader.getCompression();
        GeneralPurposeFlag generalPurposeFlag = localFileHeader.getGeneralPurposeFlag();
        new CompressionView(compression, generalPurposeFlag, offs, columnWidth).printTextInfo(out);
    }

    private void printLastModifiedTime(Out out) {
        new LastModifiedTimeView(localFileHeader.getLastModifiedTime(),
                                 offs,
                                 columnWidth,
                                 centralDirectoryEncrypted).printTextInfo(out);
    }

    private void printCrc32(Out out) {
        if (centralDirectoryEncrypted)
            printLine(out, "32-bit CRC value:", "----");
        else
            printCrc32(out, "32-bit CRC value:", localFileHeader.getCrc32());
    }

    private void printSize(Out out) {
        new SizeView("compressed size:",
                     localFileHeader.getCompressedSize(),
                     offs,
                     columnWidth,
                     false).printTextInfo(out);
        new SizeView("uncompressed size:",
                     localFileHeader.getUncompressedSize(),
                     offs,
                     columnWidth,
                     centralDirectoryEncrypted).printTextInfo(out);
    }

    private void printFileName(Out out) {
        printLine(out, "length of filename:", String.valueOf(localFileHeader.getFileName().length()));
        new StringHexView(localFileHeader.getFileName(), charset, offs, columnWidth).printTextInfo(out);
    }

    private void printExtraField(Out out) {
        ExtraField extraField = localFileHeader.getExtraField();

        if (extraField == PkwareExtraField.NULL)
            return;

        if (extraField instanceof AlignmentExtraField) {
            byte[] data = ((AlignmentExtraField) extraField).getData();
            printSize(out, "extra field (alignment):", data.length);
            new ByteArrayHexView(data, offs, columnWidth).printTextInfo(out);
        } else
            new ExtraFieldView(offs,
                               columnWidth,
                               totalDisks,
                               (PkwareExtraField) extraField,
                               localFileHeaderBlock.getExtraFieldBlock(),
                               localFileHeader.getGeneralPurposeFlag()).printLocation(out);
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
    public static final class Builder {

        private LocalFileHeader localFileHeader;
        private ZipEntryBlock.LocalFileHeaderBlock diagLocalFileHeader;
        private long totalDisks;
        private long pos;
        private Charset charset = Charsets.IBM437;
        private int offs;
        private int columnWidth;
        private boolean centralDirectoryEncrypted;

        public LocalFileHeaderView build() {
            return new LocalFileHeaderView(this);
        }

        public Builder localFileHeader(LocalFileHeader localFileHeader) {
            this.localFileHeader = localFileHeader;
            return this;
        }

        public Builder diagLocalFileHeader(ZipEntryBlock.LocalFileHeaderBlock diagLocalFileHeader) {
            this.diagLocalFileHeader = diagLocalFileHeader;
            return this;
        }

        public Builder pos(long pos) {
            this.pos = pos;
            return this;
        }

        public Builder charset(Charset charset) {
            this.charset = Optional.ofNullable(charset).orElse(Charsets.IBM437);
            return this;
        }

        public Builder centralDirectoryEncrypted(boolean centralDirectoryEncrypted) {
            this.centralDirectoryEncrypted = centralDirectoryEncrypted;
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
