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
package ru.olegcherednik.zip4jvm.view;

import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.GeneralPurposeFlag;
import ru.olegcherednik.zip4jvm.view.out.Out;

/**
 * @author Oleg Cherednik
 * @since 15.10.2019
 */
public final class CompressionView extends BaseView {

    private final Compression compression;
    private final GeneralPurposeFlag generalPurposeFlag;

    public CompressionView(Compression compression, int offs, int columnWidth) {
        this(compression, null, offs, columnWidth);
    }

    public CompressionView(Compression compression,
                           GeneralPurposeFlag generalPurposeFlag,
                           int offs,
                           int columnWidth) {
        super(offs, columnWidth);
        this.compression = compression;
        this.generalPurposeFlag = generalPurposeFlag;
    }

    @Override
    public void printTextInfo(Out out) {
        if (compression == null)
            printLine(out, "compression method (--):", "----");
        else {
            printLine(out, String.format("compression method (%02d):", compression.getCode()), compression.getTitle());
            printFileImploded(out);
            printLzma(out);
            printDeflate(out);
        }
    }

    private void printFileImploded(Out out) {
        if (generalPurposeFlag != null && compression == Compression.FILE_IMPLODED) {
            printLine(out, "  size of sliding dictionary (implosion):",
                      generalPurposeFlag.getSlidingDictionarySize().getTitle());
            printLine(out, "  number of Shannon-Fano trees (implosion):",
                      generalPurposeFlag.getShannonFanoTreesNumber().getTitle());
        }
    }

    private void printLzma(Out out) {
        if (generalPurposeFlag != null && compression == Compression.LZMA)
            printLine(out, "  end-of-stream (EOS) marker:", generalPurposeFlag.isLzmaEosMarker() ? "yes" : "no");
    }

    private void printDeflate(Out out) {
        if (generalPurposeFlag != null && (compression == Compression.DEFLATE || compression == Compression.DEFLATE_64))
            printLine(out, "  compression sub-type (deflation):", generalPurposeFlag.getCompressionLevel().getTitle());
    }

}
