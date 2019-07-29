package com.cop.zip4j.crypto.pkware;

import com.cop.zip4j.crypto.Decoder;
import com.cop.zip4j.exception.ZipException;
import com.cop.zip4j.io.LittleEndianRandomAccessFile;
import com.cop.zip4j.model.LocalFileHeader;
import lombok.NonNull;

import java.io.IOException;
import java.util.Objects;

/**
 * @author Oleg Cherednik
 * @since 22.03.2019
 */
@SuppressWarnings("MethodCanBeVariableArityMethod")
public class PkwareDecoder implements Decoder {

    private final LocalFileHeader localFileHeader;
    private final PkwareEngine engine;

    public static PkwareDecoder create(@NonNull LittleEndianRandomAccessFile in, @NonNull LocalFileHeader localFileHeader, char[] password)
            throws IOException {
        PkwareEngine engine = new PkwareEngine(password);
        in.seek(localFileHeader.getOffs());
        byte[] header = in.readBytes(PkwareEncoder.SIZE_HEADER);

        Objects.requireNonNull(header);
        decrypt(header, 0, header.length, engine);

        return new PkwareDecoder(localFileHeader, engine);
    }

    private static void decrypt(byte[] buf, int offs, int len, PkwareEngine engine) {
        for (int i = offs; i < offs + len; i++)
            buf[i] = engine.decrypt(buf[i]);
    }

    public PkwareDecoder(@NonNull LocalFileHeader localFileHeader, PkwareEngine engine) {
        this.localFileHeader = localFileHeader;
        this.engine = engine;
    }

    public PkwareDecoder(@NonNull LocalFileHeader localFileHeader, char[] password, byte[] header) {
        this.localFileHeader = localFileHeader;
        engine = new PkwareEngine(password);
        init(header);
    }

    private void init(byte[] header) {
        long crc32 = localFileHeader.getCrc32();
        // TODO check it, temporary commented
        byte[] crcBuff = new byte[4];
        crcBuff[3] = (byte)(crcBuff[3] & 0xFF);
        crcBuff[2] = (byte)((crcBuff[3] >> 8) & 0xFF);
        crcBuff[1] = (byte)((crcBuff[3] >> 16) & 0xFF);
        crcBuff[0] = (byte)((crcBuff[3] >> 24) & 0xFF);

        if (crcBuff[2] > 0 || crcBuff[1] > 0 || crcBuff[0] > 0)
            throw new IllegalStateException("Invalid CRC in File Header");

        try {
            int result = header[0];
            for (int i = 0; i < header.length; i++) {
//				Commented this as this check cannot always be trusted
//				New functionality: If there is an error in extracting a password protected file,
//				"Wrong Password?" text is appended to the exception message
//				if(i+1 == InternalZipConstants.STD_DEC_HDR_SIZE && ((byte)(result ^ zipCryptoEngine.decryptByte()) != crc32[3]) && !isSplit)
//					throw new ZipException("Wrong password!", ZipExceptionConstants.WRONG_PASSWORD);

                engine.updateKeys((byte)(result ^ engine.decrypt()));

                if (i + 1 != header.length)
                    result = header[i + 1];
            }
        } catch(Exception e) {
            throw new ZipException(e);
        }
    }

    @Override
    public int decrypt(byte[] buf, int offs, int len) {
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
