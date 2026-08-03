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
package ru.olegcherednik.zip4jvm.exception;

import ru.olegcherednik.zip4jvm.crypto.strong.EncryptionAlgorithm;
import ru.olegcherednik.zip4jvm.model.Encryption;

/**
 * @author Oleg Cherednik
 * @since 16.02.2020
 */
public class EncryptionNotSupportedException extends Zip4jvmException {

    private static final long serialVersionUID = 5827285735937278160L;

    public EncryptionNotSupportedException(Encryption encryption) {
        super(String.format("Encryption '%s' is not supported for an entry", encryption));
    }

    public EncryptionNotSupportedException(EncryptionAlgorithm encryptionAlgorithm) {
        super(String.format("Encryption '%s' is not supported for a central directory",
                            encryptionAlgorithm.getTitle()));
    }

}
