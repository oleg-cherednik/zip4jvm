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
import net.lingala.zip4j.crypto.Decoder;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.LocalFileHeader;
import net.lingala.zip4j.utils.InternalZipConstants;

/**
 * @author Oleg Cherednik
 * @since 22.03.2019
 */
public class StandardDecoder implements Decoder {

    private final LocalFileHeader localFileHeader;
    private final StandardEngine standardEngine;

    public StandardDecoder(@NonNull LocalFileHeader localFileHeader, @NonNull char[] password, byte[] headerBytes) {
        this.localFileHeader = localFileHeader;
        standardEngine = new StandardEngine(password);
        init(headerBytes);
    }

    private void init(byte[] headerBytes) {
        long crc32 = localFileHeader.getCrc32();
        // TODO check it, temporary commented
        byte[] crcBuff = new byte[4];
//        crc[3] = (byte)(crcBuff[3] & 0xFF);
//        crc[2] = (byte)((crcBuff[3] >> 8) & 0xFF);
//        crc[1] = (byte)((crcBuff[3] >> 16) & 0xFF);
//        crc[0] = (byte)((crcBuff[3] >> 24) & 0xFF);

//        if (crc[2] > 0 || crc[1] > 0 || crc[0] > 0)
//            throw new IllegalStateException("Invalid CRC in File Header");

        try {
            int result = headerBytes[0];
            for (int i = 0; i < InternalZipConstants.STD_DEC_HDR_SIZE; i++) {
//				Commented this as this check cannot always be trusted
//				New functionality: If there is an error in extracting a password protected file,
//				"Wrong Password?" text is appended to the exception message
//				if(i+1 == InternalZipConstants.STD_DEC_HDR_SIZE && ((byte)(result ^ zipCryptoEngine.decryptByte()) != crc[3]) && !isSplit)
//					throw new ZipException("Wrong password!", ZipExceptionConstants.WRONG_PASSWORD);

                standardEngine.updateKeys((byte)(result ^ standardEngine.decryptByte()));
                if (i + 1 != InternalZipConstants.STD_DEC_HDR_SIZE)
                    result = headerBytes[i + 1];
            }
        } catch(Exception e) {
            throw new ZipException(e);
        }
    }

    @Override
    public int decode(byte[] buf, int offs, int len) {
        if (offs < 0 || len < 0)
            throw new ZipException("one of the input parameters were null in standard decrpyt data");

        try {
            for (int i = offs; i < offs + len; i++) {
                int val = buf[i] & 0xff;
                val = (val ^ standardEngine.decryptByte()) & 0xff;
                standardEngine.updateKeys((byte)val);
                buf[i] = (byte)val;
            }
            return len;
        } catch(Exception e) {
            throw new ZipException(e);
        }
    }

}
