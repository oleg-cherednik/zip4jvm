package com.cop.zip4j.crypto.pkware;

import com.cop.zip4j.crypto.Decoder;
import com.cop.zip4j.exception.ZipException;
import com.cop.zip4j.model.LocalFileHeader;
import lombok.NonNull;

/**
 * @author Oleg Cherednik
 * @since 22.03.2019
 */
public class PkwareDecoder implements Decoder {

    private final LocalFileHeader localFileHeader;
    private final PkwareEngine engine;

    public PkwareDecoder(@NonNull LocalFileHeader localFileHeader, char[] password, byte[] headerBytes) {
        this.localFileHeader = localFileHeader;
        engine = new PkwareEngine(password);
        init(headerBytes);
    }

    private void init(byte[] headerBytes) {
        long crc32 = localFileHeader.getCrc32();
        // TODO check it, temporary commented
        byte[] crcBuff = new byte[4];
//        crc32[3] = (byte)(crcBuff[3] & 0xFF);
//        crc32[2] = (byte)((crcBuff[3] >> 8) & 0xFF);
//        crc32[1] = (byte)((crcBuff[3] >> 16) & 0xFF);
//        crc32[0] = (byte)((crcBuff[3] >> 24) & 0xFF);

//        if (crc32[2] > 0 || crc32[1] > 0 || crc32[0] > 0)
//            throw new IllegalStateException("Invalid CRC in File Header");

        try {
            int result = headerBytes[0];
            for (int i = 0; i < PkwareEncoder.SIZE_RND_HEADER; i++) {
//				Commented this as this check cannot always be trusted
//				New functionality: If there is an error in extracting a password protected file,
//				"Wrong Password?" text is appended to the exception message
//				if(i+1 == InternalZipConstants.STD_DEC_HDR_SIZE && ((byte)(result ^ zipCryptoEngine.decryptByte()) != crc32[3]) && !isSplit)
//					throw new ZipException("Wrong password!", ZipExceptionConstants.WRONG_PASSWORD);

                engine.updateKeys((byte)(result ^ engine.decrypt()));
                if (i + 1 != PkwareEncoder.SIZE_RND_HEADER)
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
                val = (val ^ engine.decrypt()) & 0xff;
                engine.updateKeys((byte)val);
                buf[i] = (byte)val;
            }
            return len;
        } catch(Exception e) {
            throw new ZipException(e);
        }
    }

}
