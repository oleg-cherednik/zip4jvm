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
package ru.olegcherednik.zip4jvm.view;

import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.GeneralPurposeFlag;
import ru.olegcherednik.zip4jvm.view.out.Out;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * @author Oleg Cherednik
 * @since 15.10.2019
 */
public final class GeneralPurposeFlagView extends BaseView {

    private final GeneralPurposeFlag generalPurposeFlag;
    private final Compression compression;

    public GeneralPurposeFlagView(GeneralPurposeFlag generalPurposeFlag,
                                  Compression compression,
                                  int offs,
                                  int columnWidth) {
        super(offs, columnWidth);
        this.generalPurposeFlag = generalPurposeFlag;
        this.compression = compression;

        Objects.requireNonNull(generalPurposeFlag, "'generalPurposeFlag' must not be null");
        Objects.requireNonNull(compression, "'compressionMethod' must not be null");
    }

    @Override
    public void printTextInfo(Out out) {
        int val = generalPurposeFlag.getAsInt(compression);

        printLine(out, String.format("general purpose bit flag (0x%04X) (bit 15..0):", val),
                  String.format("%s.%s %s.%s",
                                StringUtils.leftPad(Integer.toBinaryString(val >> 12 & 0xF), 4, '0'),
                                StringUtils.leftPad(Integer.toBinaryString(val >> 8 & 0xF), 4, '0'),
                                StringUtils.leftPad(Integer.toBinaryString(val >> 4 & 0xF), 4, '0'),
                                StringUtils.leftPad(Integer.toBinaryString(val & 0xF), 4, '0')));
        printLine(out, "  file security status  (bit 0):",
                  generalPurposeFlag.isEncrypted() ? "encrypted" : "not encrypted");
        printLine(out, "  data descriptor       (bit 3):",
                  generalPurposeFlag.isDataDescriptorAvailable() ? "yes" : "no");
        printLine(out, "  strong encryption     (bit 6):", generalPurposeFlag.isStrongEncryption() ? "yes" : "no");
        printLine(out, "  UTF-8 names          (bit 11):", generalPurposeFlag.isUtf8() ? "yes" : "no");
    }

}
