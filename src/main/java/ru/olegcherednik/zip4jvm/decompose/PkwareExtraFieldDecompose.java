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

import ru.olegcherednik.zip4jvm.model.GeneralPurposeFlag;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.block.ExtraFieldBlock;
import ru.olegcherednik.zip4jvm.model.extrafield.PkwareExtraField;
import ru.olegcherednik.zip4jvm.utils.PathUtils;
import ru.olegcherednik.zip4jvm.view.extrafield.ExtraFieldView;
import ru.olegcherednik.zip4jvm.view.out.Out;

import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 07.12.2019
 */
public final class PkwareExtraFieldDecompose implements Decompose {

    private final ZipModel zipModel;
    private final PkwareExtraField extraField;
    private final ExtraFieldBlock block;
    private final GeneralPurposeFlag generalPurposeFlag;
    private final int offs;
    private final int columnWidth;

    public PkwareExtraFieldDecompose(ZipModel zipModel,
                                     PkwareExtraField extraField,
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
    public void printTextInfoNew(Out out) {
        if (extraField != PkwareExtraField.NULL)
            createView().printTextInfo(out);
    }

    @Override
    public Path decompose(Path dir) {
        if (extraField == PkwareExtraField.NULL)
            return dir;

        Path newDir = PathUtils.createDirectories(dir.resolve("extra_fields"));
        ExtraFieldView view = createView();

        for (int signature : extraField.getSignatures()) {
            view.getView(extraField.getRecord(signature)).ifPresent(recordView -> {
                String fileName = recordView.getFileName();
                Utils.print(newDir.resolve(fileName + EXT_TXT), recordView::printTextInfo);
                block.getRecord(signature).copyLarge(zipModel, newDir.resolve(fileName + EXT_DATA));
            });
        }

        return dir;
    }

    private ExtraFieldView createView() {
        return new ExtraFieldView(offs,
                                  columnWidth,
                                  zipModel.getTotalDisks(),
                                  extraField,
                                  block,
                                  generalPurposeFlag);
    }

}
