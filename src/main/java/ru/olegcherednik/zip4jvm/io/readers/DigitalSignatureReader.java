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
package ru.olegcherednik.zip4jvm.io.readers;

import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.utils.function.Reader;

/**
 * @author Oleg Cherednik
 * @since 13.04.2019
 */
public class DigitalSignatureReader implements Reader<CentralDirectory.DigitalSignature> {

    @Override
    public final CentralDirectory.DigitalSignature read(DataInput in) {
        return findSignature(in) ? readDigitalSignature(in) : null;
    }

    protected CentralDirectory.DigitalSignature readDigitalSignature(DataInput in) {
        in.skip(in.dwordSignatureSize());

        CentralDirectory.DigitalSignature digitalSignature = new CentralDirectory.DigitalSignature();
        digitalSignature.setSignatureData(in.readBytes(in.readWord()));

        return digitalSignature;
    }

    private static boolean findSignature(DataInput in) {
        try {
            boolean exists = in.readDwordSignature() == CentralDirectory.DigitalSignature.SIGNATURE;
            in.backward(in.dwordSignatureSize());
            return exists;
        } catch (Exception e) {
            // TODO should be IOException here; e.g. byte array does not have bytes more to read
            return false;
        }
    }
}
