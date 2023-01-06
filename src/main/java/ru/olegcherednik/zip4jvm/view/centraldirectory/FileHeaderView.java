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
package ru.olegcherednik.zip4jvm.view.centraldirectory;

import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.CompressionMethod;
import ru.olegcherednik.zip4jvm.model.extrafield.PkwareExtraField;
import ru.olegcherednik.zip4jvm.model.GeneralPurposeFlag;
import ru.olegcherednik.zip4jvm.model.block.CentralDirectoryBlock;
import ru.olegcherednik.zip4jvm.view.BaseView;
import ru.olegcherednik.zip4jvm.view.CompressionMethodView;
import ru.olegcherednik.zip4jvm.view.ExternalFileAttributesView;
import ru.olegcherednik.zip4jvm.view.GeneralPurposeFlagView;
import ru.olegcherednik.zip4jvm.view.InternalFileAttributesView;
import ru.olegcherednik.zip4jvm.view.LastModifiedTimeView;
import ru.olegcherednik.zip4jvm.view.SizeView;
import ru.olegcherednik.zip4jvm.view.StringHexView;
import ru.olegcherednik.zip4jvm.view.VersionView;
import ru.olegcherednik.zip4jvm.view.extrafield.ExtraFieldView;

import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.Optional;

/**
 * @author Oleg Cherednik
 * @since 14.10.2019
 */
public class FileHeaderView extends BaseView {

    protected final CentralDirectory.FileHeader fileHeader;
    protected final CentralDirectoryBlock.FileHeaderBlock block;
    protected final long pos;
    protected final Charset charset;

    public FileHeaderView(CentralDirectory.FileHeader fileHeader,
                          CentralDirectoryBlock.FileHeaderBlock block,
                          long pos,
                          Charset charset,
                          int offs,
                          int columnWidth,
                          long totalDisks) {
        super(offs, columnWidth, totalDisks);
        this.fileHeader = fileHeader;
        this.block = block;
        this.pos = pos;
        this.charset = charset;
    }

    @Override
    public boolean printTextInfo(PrintStream out) {
        printTitle(out);
        printLocation(out);
        printVersion(out);
        printGeneralPurposeFlag(out);
        printCompressionMethod(out);
        printLastModifiedTime(out);
        printCrc(out);
        printSize(out);
        printFileName(out);
        printComment(out);
        printInternalFileAttributesView(out);
        printExternalFileAttributes(out);
        printExtraField(out);
        return true;
    }

    private void printTitle(PrintStream out) {
        printSubTitle(out, CentralDirectory.FileHeader.SIGNATURE, pos, '[' + charset.name() + "] " + fileHeader.getFileName(), block);
    }

    private void printLocation(PrintStream out) {
        printLine(out, String.format("part number of this part (%04X):", fileHeader.getDiskNo()), String.valueOf(fileHeader.getDiskNo() + 1));
        printLine(out, "relative offset of local header:", String.format("%1$d (0x%1$08X) bytes", fileHeader.getLocalFileHeaderRelativeOffs()));
    }

    private void printVersion(PrintStream out) {
        new VersionView(fileHeader.getVersionMadeBy(), fileHeader.getVersionToExtract(), offs, columnWidth).printTextInfo(out);
    }

    private void printGeneralPurposeFlag(PrintStream out) {
        new GeneralPurposeFlagView(fileHeader.getGeneralPurposeFlag(), fileHeader.getCompressionMethod(), offs, columnWidth).printTextInfo(out);
    }

    private void printCompressionMethod(PrintStream out) {
        CompressionMethod compressionMethod = fileHeader.getCompressionMethod();
        GeneralPurposeFlag generalPurposeFlag = fileHeader.getGeneralPurposeFlag();
        new CompressionMethodView(compressionMethod, generalPurposeFlag, offs, columnWidth).printTextInfo(out);
    }

    private void printLastModifiedTime(PrintStream out) {
        new LastModifiedTimeView(fileHeader.getLastModifiedTime(), offs, columnWidth).printTextInfo(out);
    }

    private void printCrc(PrintStream out) {
        printLine(out, "32-bit CRC value:", String.format("0x%08X", fileHeader.getCrc32()));
    }

    private void printSize(PrintStream out) {
        new SizeView("compressed size:", fileHeader.getCompressedSize(), offs, columnWidth).printTextInfo(out);
        new SizeView("uncompressed size:", fileHeader.getUncompressedSize(), offs, columnWidth).printTextInfo(out);
    }

    private void printFileName(PrintStream out) {
        printLine(out, "length of filename:", String.valueOf(fileHeader.getFileName().length()));
        new StringHexView(fileHeader.getFileName(), charset, offs, columnWidth).printTextInfo(out);
    }

    private void printComment(PrintStream out) {
        String comment = Optional.ofNullable(fileHeader.getComment()).orElse("");
        printLine(out, "length of file comment:", String.format("%d bytes", comment.getBytes(charset).length));
        new StringHexView(fileHeader.getComment(), charset, offs, columnWidth).printTextInfo(out);
    }

    private void printInternalFileAttributesView(PrintStream out) {
        new InternalFileAttributesView(fileHeader.getInternalFileAttributes(), offs, columnWidth).printTextInfo(out);
    }

    private void printExternalFileAttributes(PrintStream out) {
        new ExternalFileAttributesView(fileHeader.getExternalFileAttributes(), offs, columnWidth).printTextInfo(out);
    }

    private void printExtraField(PrintStream out) {
        if (fileHeader.getExtraField() == PkwareExtraField.NULL)
            return;

        new ExtraFieldView(fileHeader.getExtraField(),
                           block.getExtraFieldBlock(),
                           fileHeader.getGeneralPurposeFlag(),
                           offs,
                           columnWidth,
                           totalDisks).printLocation(out);
    }

}
