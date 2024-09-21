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
package ru.olegcherednik.zip4jvm.io.readers.zip64;

import lombok.AllArgsConstructor;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.extrafield.PkwareExtraField;
import ru.olegcherednik.zip4jvm.utils.function.ReaderWithSize;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.realBigZip64;

/**
 * @author Oleg Cherednik
 * @since 29.12.2022
 */
@AllArgsConstructor
public class ExtendedInfoReader implements ReaderWithSize<Zip64.ExtendedInfo> {

    private boolean uncompressedSizeExists;
    private boolean compressedSizeExists;
    private boolean offsLocalHeaderRelativeExists;
    private boolean diskExists;

    private void updateFlags(DataInput in, int size) {
        if (uncompressedSizeExists || compressedSizeExists || offsLocalHeaderRelativeExists || diskExists)
            return;

        uncompressedSizeExists = size >= in.qwordSize();
        compressedSizeExists = size >= in.qwordSize() * 2;
        offsLocalHeaderRelativeExists = size >= in.qwordSize() * 3;
        diskExists = size >= in.qwordSize() * 3 + in.dwordSize();
    }

    @Override
    public Zip64.ExtendedInfo read(DataInput in, int size) {
        long offs = in.getAbsoluteOffs();
        updateFlags(in, size);

        Zip64.ExtendedInfo extendedInfo = readExtendedInfo(in);

        if (in.getAbsoluteOffs() - offs != size) {
            // section exists, but not need to read it; all data is in FileHeader
            extendedInfo = Zip64.ExtendedInfo.NULL;
            in.seek(offs + size);
        }

        if (extendedInfo.getDiskNo() != PkwareExtraField.NO_DATA)
            realBigZip64(extendedInfo.getDiskNo(), "zip64.extendedInfo.disk");

        return extendedInfo;
    }

    private Zip64.ExtendedInfo readExtendedInfo(DataInput in) {
        return Zip64.ExtendedInfo.builder()
                                 .uncompressedSize(uncompressedSizeExists ? in.readQword() : PkwareExtraField.NO_DATA)
                                 .compressedSize(compressedSizeExists ? in.readQword() : PkwareExtraField.NO_DATA)
                                 .localFileHeaderRelativeOffs(
                                         offsLocalHeaderRelativeExists ? in.readQword() : PkwareExtraField.NO_DATA)
                                 .diskNo(diskExists ? in.readDword() : PkwareExtraField.NO_DATA)
                                 .build();
    }

    @Override
    public String toString() {
        return "ZIP64";
    }

}
