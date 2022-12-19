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

import ru.olegcherednik.zip4jvm.model.CompressionMethod;
import ru.olegcherednik.zip4jvm.model.GeneralPurposeFlag;

import java.io.PrintStream;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireNotNull;

/**
 * @author Oleg Cherednik
 * @since 15.10.2019
 */
public final class CompressionMethodView extends BaseView {

    private final CompressionMethod compressionMethod;
    private final GeneralPurposeFlag generalPurposeFlag;

    public CompressionMethodView(int offs, int columnWidth) {
        this(null, null, offs, columnWidth);
    }

    public CompressionMethodView(CompressionMethod compressionMethod, int offs, int columnWidth) {
        this(compressionMethod, null, offs, columnWidth);
    }

    public CompressionMethodView(CompressionMethod compressionMethod, GeneralPurposeFlag generalPurposeFlag, int offs, int columnWidth) {
        super(offs, columnWidth);
        this.compressionMethod = compressionMethod;
        this.generalPurposeFlag = generalPurposeFlag;
    }

    @Override
    public boolean print(PrintStream out) {
        if (compressionMethod == null)
            printLine(out, "compression method (--):", "----");
        else {
            printLine(out, String.format("compression method (%02d):", compressionMethod.getCode()), compressionMethod.getTitle());

            if (generalPurposeFlag != null) {
                if (compressionMethod == CompressionMethod.FILE_IMPLODED) {
                    printLine(out, "  size of sliding dictionary (implosion):", generalPurposeFlag.getSlidingDictionarySize().getTitle());
                    printLine(out, "  number of Shannon-Fano trees (implosion):", generalPurposeFlag.getShannonFanoTreesNumber().getTitle());
                } else if (compressionMethod == CompressionMethod.LZMA)
                    printLine(out, "  end-of-stream (EOS) marker:", generalPurposeFlag.isLzmaEosMarker() ? "yes" : "no");
                else if (compressionMethod == CompressionMethod.DEFLATE || compressionMethod == CompressionMethod.ENHANCED_DEFLATE)
                    printLine(out, "  compression sub-type (deflation):", generalPurposeFlag.getCompressionLevel().getTitle());
            }
        }

        return true;
    }

}
