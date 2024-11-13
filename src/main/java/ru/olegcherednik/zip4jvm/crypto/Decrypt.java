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
package ru.olegcherednik.zip4jvm.crypto;

import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;

/**
 * @author Oleg Cherednik
 * @since 05.12.2022
 */
public interface Decrypt {

    default int decrypt(byte[] buf, int offs, int len) {
        try {
            for (int i = 0; i < len; i++)
                buf[offs + i] = decrypt(buf[offs + i]);

            return len;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new Zip4jvmException(e);
        }
    }

    default byte decrypt(byte b) {
        byte[] buf = new byte[1];
        decrypt(buf, 0, 1);
        return buf[0];
    }

}
