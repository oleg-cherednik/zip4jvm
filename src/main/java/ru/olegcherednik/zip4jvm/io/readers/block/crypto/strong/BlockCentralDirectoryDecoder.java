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
package ru.olegcherednik.zip4jvm.io.readers.block.crypto.strong;

import ru.olegcherednik.zip4jvm.crypto.strong.cd.CentralDirectoryDecoder;
import ru.olegcherednik.zip4jvm.model.block.crypto.EncryptedCentralDirectoryBlock;

import java.util.Arrays;
import javax.crypto.Cipher;

/**
 * @author Oleg Cherednik
 * @since 27.09.2024
 */
public class BlockCentralDirectoryDecoder extends CentralDirectoryDecoder {

    private final EncryptedCentralDirectoryBlock block;

    public BlockCentralDirectoryDecoder(Cipher cipher, EncryptedCentralDirectoryBlock block) {
        super(cipher);
        this.block = block;
    }

    @Override
    public int decrypt(byte[] buf, int offs, int len) {
        int readNow = super.decrypt(buf, offs, len);
        byte[] arr = new byte[readNow];
        System.arraycopy(buf, offs, arr, 0, readNow);
        block.setDecompressedCentralDirectory(arr);
        return readNow;
    }

}
