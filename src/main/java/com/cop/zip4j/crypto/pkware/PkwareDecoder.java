package com.cop.zip4j.crypto.pkware;

import com.cop.zip4j.crypto.Decoder;
import com.cop.zip4j.exception.Zip4jException;
import com.cop.zip4j.io.in.DataInput;
import com.cop.zip4j.model.CentralDirectory;
import com.cop.zip4j.model.LocalFileHeader;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 22.03.2019
 */
@RequiredArgsConstructor
@SuppressWarnings("MethodCanBeVariableArityMethod")
public final class PkwareDecoder implements Decoder {

    private final PkwareEngine engine;

    public static PkwareDecoder create(@NonNull DataInput in, @NonNull LocalFileHeader localFileHeader, char[] password)
            throws IOException {
        PkwareEngine engine = new PkwareEngine(password);
        PkwareHeader.read(in, localFileHeader, engine);
        return new PkwareDecoder(engine);
    }

    @Override
    public void decrypt(byte[] buf, int offs, int len) {
        engine.decrypt(buf, offs, len);
    }

    @Override
    public void checkChecksum(@NonNull CentralDirectory.FileHeader fileHeader, long checksum) {
        if (checksum != fileHeader.getCrc32())
            throw new Zip4jException("Incorrect checksum filename '" + fileHeader.getFileName() + '\'');
    }

    @Override
    public long getCompressedSize(@NonNull LocalFileHeader localFileHeader) {
        return localFileHeader.getCompressedSize() - PkwareHeader.SIZE;
    }

    @Override
    public long getOffs(@NonNull LocalFileHeader localFileHeader) {
        return localFileHeader.getOffs() + PkwareHeader.SIZE;
    }

}
