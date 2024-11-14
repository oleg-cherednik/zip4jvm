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
package ru.olegcherednik.zip4jvm.io.in.data.ecd;

import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.ed.EnhancedDeflateInputStream;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 20.12.2022
 */
final class EnhancedDeflateDataInput extends CompressedEcdDataInput {

    EnhancedDeflateDataInput(DataInput in, int uncompressedSize) {
        super(read(in, uncompressedSize), in.getByteOrder());
    }

    private static byte[] read(DataInput in, int uncompressedSize) {
        try (EnhancedDeflateInputStream bzip = new EnhancedDeflateInputStream(in)) {
            byte[] buf = new byte[uncompressedSize];
            bzip.read(buf, 0, buf.length);
            return buf;
        } catch (IOException e) {
            throw new Zip4jvmException(e);
        }
    }

}