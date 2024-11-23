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
package ru.olegcherednik.zip4jvm.io.in.buf;

import ru.olegcherednik.zip4jvm.io.ByteOrder;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;

import lombok.Getter;

/**
 * This class was designed only to cover one decompose test split+ecd.
 * It should be removed and real problem should be fixed.
 *
 * @author Oleg Cherednik
 * @since 08.01.2023
 * @deprecated this is a temporary fix
 */
@Deprecated
public final class DiskByteArrayDataInput extends ByteArrayDataInput {

    @Getter
    private final SrcZip.Disk disk;

    public DiskByteArrayDataInput(byte[] buf, ByteOrder byteOrder, SrcZip.Disk disk) {
        super(buf, byteOrder, 0, 0);
        this.disk = disk;
    }

}
