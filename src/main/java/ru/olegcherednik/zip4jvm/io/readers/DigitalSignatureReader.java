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
package ru.olegcherednik.zip4jvm.io.readers;

import ru.olegcherednik.zip4jvm.exception.SignatureNotFoundException;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.io.in.file.random.RandomAccessDataInput;
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
        checkSignature(in);

        CentralDirectory.DigitalSignature digitalSignature = new CentralDirectory.DigitalSignature();
        digitalSignature.setSignatureData(in.readBytes(in.readWord()));

        return digitalSignature;
    }

    private static void checkSignature(DataInput in) {
        long offs = in.getAbsOffs();

        if (in.readDwordSignature() != CentralDirectory.DigitalSignature.SIGNATURE)
            throw new SignatureNotFoundException(CentralDirectory.DigitalSignature.SIGNATURE,
                                                 "CentralDirectory.DigitalSignature",
                                                 offs);
    }

    private static boolean findSignature(DataInput in) {
        try {
            // TODO durty hack
            return ((RandomAccessDataInput) in).isDwordSignature(CentralDirectory.DigitalSignature.SIGNATURE);
        } catch (Exception e) {
            // TODO should be IOException here; e.g. byte array does not have bytes more to read
            return false;
        }
    }
}
