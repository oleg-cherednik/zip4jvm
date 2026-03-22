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

import ru.olegcherednik.zip4jvm.model.GeneralPurposeFlag;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.block.ExtraFieldBlock;
import ru.olegcherednik.zip4jvm.model.extrafield.PkwareExtraField;
import ru.olegcherednik.zip4jvm.view.View;
import ru.olegcherednik.zip4jvm.view.out.Out;

/**
 * @author Oleg Cherednik
 * @since 22.03.2026
 */
public final class PkwareExtraFieldView implements View {

    private final ZipModel zipModel;
    private final PkwareExtraField extraField;
    private final ExtraFieldBlock block;
    private final GeneralPurposeFlag generalPurposeFlag;
    private final int offs;
    private final int columnWidth;

    public PkwareExtraFieldView(ZipModel zipModel,
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

    public ExtraFieldView createExtraFieldView() {
        return new ExtraFieldView(offs,
                                  columnWidth,
                                  zipModel.getTotalDisks(),
                                  extraField,
                                  block,
                                  generalPurposeFlag);
    }

    // ---------- View ----------

    @Override
    public void printTextInfo(Out out) {
        if (extraField != PkwareExtraField.NULL)
            createExtraFieldView().printTextInfo(out);
    }


}
