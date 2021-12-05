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
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.view.BaseView;
import ru.olegcherednik.zip4jvm.view.ByteArrayHexView;

import java.io.PrintStream;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireNotNull;

/**
 * @author Oleg Cherednik
 * @since 18.10.2019
 */
public final class DigitalSignatureView extends BaseView {

    private final CentralDirectory.DigitalSignature digitalSignature;
    private final Block block;

    public DigitalSignatureView(CentralDirectory.DigitalSignature digitalSignature, Block block, int offs, int columnWidth, long totalDisks) {
        super(offs, columnWidth, totalDisks);
        this.digitalSignature = requireNotNull(digitalSignature, "DigitalSignatureView.centralDirectory");
        this.block = requireNotNull(block, "DigitalSignatureView.block");
    }

    @Override
    public boolean print(PrintStream out) {
        printTitle(out, CentralDirectory.DigitalSignature.SIGNATURE, "Digital signature", block);
        return new ByteArrayHexView(digitalSignature.getSignatureData(), offs, columnWidth).print(out);
    }
}
