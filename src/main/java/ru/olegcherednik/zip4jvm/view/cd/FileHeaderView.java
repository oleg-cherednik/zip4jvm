/*
 * Copyright 2019 Oleg Cherednik (oleg.cherednik@gmail.com)
 *
 * Licensed under The Apache Software License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
package ru.olegcherednik.zip4jvm.view.cd;

import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.GeneralPurposeFlag;
import ru.olegcherednik.zip4jvm.model.block.CentralDirectoryBlock;
import ru.olegcherednik.zip4jvm.model.extrafield.PkwareExtraField;
import ru.olegcherednik.zip4jvm.view.BaseView;
import ru.olegcherednik.zip4jvm.view.CompressionView;
import ru.olegcherednik.zip4jvm.view.ExternalFileAttributesView;
import ru.olegcherednik.zip4jvm.view.GeneralPurposeFlagView;
import ru.olegcherednik.zip4jvm.view.InternalFileAttributesView;
import ru.olegcherednik.zip4jvm.view.LastModifiedTimeView;
import ru.olegcherednik.zip4jvm.view.SizeView;
import ru.olegcherednik.zip4jvm.view.StringHexView;
import ru.olegcherednik.zip4jvm.view.VersionView;
import ru.olegcherednik.zip4jvm.view.extrafield.ExtraFieldView;
import ru.olegcherednik.zip4jvm.view.out.Out;

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

    // ---------- View ----------

    @Override
    public void printTextInfo(Out out) {
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
    }

    // ----------

    private void printTitle(Out out) {
        int signature = CentralDirectory.FileHeader.SIGNATURE;
        String title = '[' + charset.name() + "] " + fileHeader.getFileName();
        printSubTitle(out, signature, pos, title, block);
    }

    private void printLocation(Out out) {
        printLine(out, String.format("part number of this part (%04X):", fileHeader.getDiskNo()),
                  String.valueOf(fileHeader.getDiskNo() + 1));
        printOffs(out, "relative offset of local header:", fileHeader.getLocalFileHeaderRelativeOffs());
    }

    private void printVersion(Out out) {
        new VersionView(fileHeader.getVersionMadeBy(),
                        fileHeader.getVersionToExtract(),
                        offs,
                        columnWidth).printTextInfo(out);
    }

    private void printGeneralPurposeFlag(Out out) {
        new GeneralPurposeFlagView(fileHeader.getGeneralPurposeFlag(),
                                   fileHeader.getCompression(),
                                   offs,
                                   columnWidth).printTextInfo(out);
    }

    private void printCompressionMethod(Out out) {
        Compression compression = fileHeader.getCompression();
        GeneralPurposeFlag generalPurposeFlag = fileHeader.getGeneralPurposeFlag();
        new CompressionView(compression, generalPurposeFlag, offs, columnWidth).printTextInfo(out);
    }

    private void printLastModifiedTime(Out out) {
        new LastModifiedTimeView(fileHeader.getLastModifiedTime(), offs, columnWidth).printTextInfo(out);
    }

    private void printCrc(Out out) {
        printCrc32(out, "32-bit CRC value:", fileHeader.getCrc32());
    }

    private void printSize(Out out) {
        new SizeView("compressed size:", fileHeader.getCompressedSize(), offs, columnWidth).printTextInfo(out);
        new SizeView("uncompressed size:", fileHeader.getUncompressedSize(), offs, columnWidth).printTextInfo(out);
    }

    private void printFileName(Out out) {
        printLine(out, "length of filename:", String.valueOf(fileHeader.getFileName().length()));
        new StringHexView(fileHeader.getFileName(), charset, offs, columnWidth).printTextInfo(out);
    }

    private void printComment(Out out) {
        String comment = Optional.ofNullable(fileHeader.getComment()).orElse("");
        printSize(out, "length of file comment:", comment.getBytes(charset).length);
        new StringHexView(fileHeader.getComment(), charset, offs, columnWidth).printTextInfo(out);
    }

    private void printInternalFileAttributesView(Out out) {
        new InternalFileAttributesView(fileHeader.getInternalFileAttributes(), offs, columnWidth).printTextInfo(out);
    }

    private void printExternalFileAttributes(Out out) {
        new ExternalFileAttributesView(fileHeader.getExternalFileAttributes(), offs, columnWidth).printTextInfo(out);
    }

    private void printExtraField(Out out) {
        if (fileHeader.getExtraField() == PkwareExtraField.NULL)
            return;

        new ExtraFieldView(offs,
                           columnWidth,
                           totalDisks,
                           fileHeader.getExtraField(),
                           block.getExtraFieldBlock(),
                           fileHeader.getGeneralPurposeFlag()).printLocation(out);
    }

}
