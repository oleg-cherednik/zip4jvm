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

import ru.olegcherednik.zip4jvm.io.readers.extrafiled.ExtraFieldReader;
import ru.olegcherednik.zip4jvm.model.extrafield.AlignmentExtraField;
import ru.olegcherednik.zip4jvm.model.extrafield.ExtraField;
import ru.olegcherednik.zip4jvm.model.GeneralPurposeFlag;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.block.ExtraFieldBlock;
import ru.olegcherednik.zip4jvm.model.extrafield.IExtraField;
import ru.olegcherednik.zip4jvm.view.extrafield.ExtraFieldRecordView;
import ru.olegcherednik.zip4jvm.view.extrafield.ExtraFieldView;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 07.12.2019
 */
public final class ExtraFieldDecompose implements Decompose {

    private final ZipModel zipModel;
    private final ExtraField extraField;
    private final ExtraFieldBlock block;
    private final GeneralPurposeFlag generalPurposeFlag;
    private final int offs;
    private final int columnWidth;

    public ExtraFieldDecompose(ZipModel zipModel,
                               ExtraField extraField,
                               ExtraFieldBlock block,
                               GeneralPurposeFlag generalPurposeFlag,
                               int offs,
                               int columnWidth) {
        this.zipModel = zipModel;
        this.extraField = extraField;
        this.block = block;
        this.generalPurposeFlag = generalPurposeFlag;
        this.offs = offs;
        this.columnWidth = columnWidth;
    }

    @Override
    public boolean printTextInfo(PrintStream out, boolean emptyLine) {
        return extraField != ExtraField.NULL && createView().print(out, emptyLine);
    }

    @Override
    public Path decompose(Path dir) throws IOException {
        if (extraField == ExtraField.NULL)
            return dir;

        dir = Files.createDirectories(dir.resolve("extra_fields"));
        ExtraFieldView view = createView();

        for (int signature : extraField.getSignatures()) {
            ExtraFieldRecordView<?> recordView = view.getView(extraField.getRecord(signature));
            String fileName = recordView.getFileName();

            Utils.print(dir.resolve(fileName + ".txt"), recordView::print);
            block.getRecord(signature).copyLarge(zipModel, dir.resolve(fileName + ".data"));
        }

        return dir;
    }

    private ExtraFieldView createView() {
        return new ExtraFieldView(extraField,
                                  block,
                                  generalPurposeFlag,
                                  offs,
                                  columnWidth,
                                  zipModel.getTotalDisks());
    }

}
