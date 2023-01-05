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
package ru.olegcherednik.zip4jvm.decompose;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.extrafield.ExtraField;
import ru.olegcherednik.zip4jvm.model.GeneralPurposeFlag;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.block.BaseCentralDirectoryBlock;
import ru.olegcherednik.zip4jvm.model.block.CentralDirectoryBlock;
import ru.olegcherednik.zip4jvm.model.block.ExtraFieldBlock;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;
import ru.olegcherednik.zip4jvm.view.centraldirectory.FileHeaderView;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 08.12.2019
 */
@RequiredArgsConstructor
public class FileHeaderDecompose implements Decompose {

    protected final ZipModel zipModel;
    protected final ZipInfoSettings settings;
    private final CentralDirectory centralDirectory;
    private final BaseCentralDirectoryBlock block;

    @Override
    public boolean printTextInfo(PrintStream out, boolean emptyLine) {
        long pos = 0;

        for (CentralDirectory.FileHeader fileHeader : centralDirectory.getFileHeaders()) {
            CentralDirectoryBlock.FileHeaderBlock fileHeaderBlock = block.getFileHeader(fileHeader.getFileName());

            emptyLine |= fileHeaderView(fileHeader, fileHeaderBlock, pos).print(out, pos != 0 || emptyLine);
            emptyLine |= extraFields(fileHeader, fileHeaderBlock.getExtraFieldBlock(), settings.getOffs()).printTextInfo(out, false);

            pos++;
        }

        return emptyLine;
    }

    @Override
    public Path decompose(Path dir) throws IOException {
        long pos = 0;

        for (CentralDirectory.FileHeader fileHeader : centralDirectory.getFileHeaders()) {
            String fileName = fileHeader.getFileName();
            CentralDirectoryBlock.FileHeaderBlock fileHeaderBlock = block.getFileHeader(fileName);
            Path subDir = Utils.createSubDir(dir, zipModel.getZipEntryByFileName(fileName), pos);

            fileHeader(subDir, fileHeader, fileHeaderBlock, pos);
            extraFields(fileHeader, fileHeaderBlock.getExtraFieldBlock(), 0).decompose(subDir);

            pos++;
        }

        return dir;
    }

    private void fileHeader(Path dir,
                            CentralDirectory.FileHeader fileHeader,
                            CentralDirectoryBlock.FileHeaderBlock block,
                            long pos) throws IOException {
        String fileName = "file_header";

        Utils.print(dir.resolve(fileName + ".txt"), out -> fileHeaderView(fileHeader, block, pos).print(out));
        block.copyLarge(zipModel, dir.resolve(fileName + ".data"));
    }

    protected FileHeaderView fileHeaderView(CentralDirectory.FileHeader fileHeader,
                                            CentralDirectoryBlock.FileHeaderBlock block,
                                            long pos) {
        return new FileHeaderView(fileHeader,
                                  block,
                                  pos,
                                  settings.getCharset(),
                                  settings.getOffs(),
                                  settings.getColumnWidth(),
                                  zipModel.getTotalDisks());
    }

    private ExtraFieldDecompose extraFields(CentralDirectory.FileHeader fileHeader,
                                            ExtraFieldBlock block,
                                            int offs) {
        ExtraField extraField = fileHeader.getExtraField();
        GeneralPurposeFlag generalPurposeFlag = fileHeader.getGeneralPurposeFlag();
        return new ExtraFieldDecompose(zipModel, extraField, block, generalPurposeFlag, offs, settings.getColumnWidth());
    }

}
