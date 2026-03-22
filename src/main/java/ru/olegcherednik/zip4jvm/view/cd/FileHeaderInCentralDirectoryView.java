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
package ru.olegcherednik.zip4jvm.view.cd;

import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.block.BaseCentralDirectoryBlock;
import ru.olegcherednik.zip4jvm.model.block.CentralDirectoryBlock;
import ru.olegcherednik.zip4jvm.model.block.ExtraFieldBlock;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;
import ru.olegcherednik.zip4jvm.view.View;
import ru.olegcherednik.zip4jvm.view.extrafield.PkwareExtraFieldView;
import ru.olegcherednik.zip4jvm.view.out.Out;

import lombok.RequiredArgsConstructor;

/**
 * @author Oleg Cherednik
 * @since 22.03.2026
 */
@RequiredArgsConstructor
public class FileHeaderInCentralDirectoryView implements View {

    protected final ZipModel zipModel;
    protected final ZipInfoSettings settings;
    private final CentralDirectory centralDirectory;
    private final BaseCentralDirectoryBlock block;

    // ---------- View ----------

    @Override
    public void printTextInfo(Out out) {
        long pos = 0;

        for (CentralDirectory.FileHeader fileHeader : centralDirectory.getFileHeaders()) {
            CentralDirectoryBlock.FileHeaderBlock fileHeaderBlock = block.getFileHeader(fileHeader.getFileName());

            out.printEmptyLine();
            fileHeaderView(fileHeader, fileHeaderBlock, pos).printTextInfo(out);
            extraFieldView(fileHeader, fileHeaderBlock.getExtraFieldBlock(), settings.getOffs()).printTextInfo(out);

            pos++;
        }
    }

    // ----------

    public FileHeaderView fileHeaderView(CentralDirectory.FileHeader fileHeader,
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

    private PkwareExtraFieldView extraFieldView(CentralDirectory.FileHeader fileHeader,
                                                ExtraFieldBlock block,
                                                int offs) {
        return new PkwareExtraFieldView(zipModel,
                                        fileHeader.getExtraField(),
                                        block,
                                        fileHeader.getGeneralPurposeFlag(),
                                        offs,
                                        settings.getColumnWidth());
    }

}
