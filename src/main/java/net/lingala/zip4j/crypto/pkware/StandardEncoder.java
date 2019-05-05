/*
 * Copyright 2010 Srikanth Reddy Lingala
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.lingala.zip4j.crypto.pkware;

import lombok.NonNull;
import net.lingala.zip4j.crypto.Encoder;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.SplitOutputStream;
import net.lingala.zip4j.utils.InternalZipConstants;
import org.apache.commons.lang.ArrayUtils;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 22.03.2019
 */
public class StandardEncoder implements Encoder {

    private final StandardEngine standardEngine;
    private final byte[] headerBytes = new byte[InternalZipConstants.STD_DEC_HDR_SIZE];

    public StandardEncoder(char[] password, int crc) {
        if (ArrayUtils.isEmpty(password))
            throw new ZipException("input password is null or empty in StandardEncoder constructor");

        standardEngine = new StandardEngine(password);
        init(crc);
    }

    private void init(int crc) {
        headerBytes[InternalZipConstants.STD_DEC_HDR_SIZE - 1] = (byte)((crc >>> 24));
        headerBytes[InternalZipConstants.STD_DEC_HDR_SIZE - 2] = (byte)((crc >>> 16));
        encode(headerBytes);
    }

    @Override
    public void encode(byte[] buf, int offs, int len) throws ZipException {

        if (len < 0) {
            throw new ZipException("invalid length specified to decrpyt data");
        }

        try {
            for (int i = offs; i < offs + len; i++)
                buf[i] = standardEngine.encrypt(buf[i]);
        } catch(Exception e) {
            throw new ZipException(e);
        }
    }

    @Override
    public void write(@NonNull SplitOutputStream out) throws IOException {
        out.writeBytes(headerBytes);
    }

}
