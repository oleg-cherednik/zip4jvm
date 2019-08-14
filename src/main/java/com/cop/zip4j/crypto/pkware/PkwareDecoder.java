package com.cop.zip4j.crypto.pkware;

import com.cop.zip4j.crypto.Decoder;
import com.cop.zip4j.io.in.DataInput;
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
    public void decrypt(@NonNull byte[] buf, int offs, int len) {
        engine.decrypt(buf, offs, len);
    }

    @Override
    public long getCompressedSize(@NonNull LocalFileHeader localFileHeader) {
        return localFileHeader.getCompressedSize() - PkwareHeader.SIZE;
    }

}
