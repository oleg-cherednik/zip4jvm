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
package ru.olegcherednik.zip4jvm.io.writers;

import ru.olegcherednik.zip4jvm.io.out.DataOutput;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.utils.function.Writer;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 13.04.2019
 */
@RequiredArgsConstructor
final class DigitalSignatureWriter implements Writer {

    private final CentralDirectory.DigitalSignature digitalSignature;

    // ---------- Writer ----------

    @Override
    public void write(DataOutput out) throws IOException {
        if (digitalSignature == null)
            return;

        out.writeDwordSignature(CentralDirectory.DigitalSignature.SIGNATURE);
        out.writeWord(ArrayUtils.getLength(digitalSignature.getSignatureData()));
        out.writeBytes(digitalSignature.getSignatureData());
    }

}
