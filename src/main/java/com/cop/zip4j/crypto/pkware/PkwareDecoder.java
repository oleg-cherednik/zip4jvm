package com.cop.zip4j.crypto.pkware;

import com.cop.zip4j.crypto.Decoder;
import com.cop.zip4j.io.in.DataInput;
import com.cop.zip4j.model.entry.PathZipEntry;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 22.03.2019
 */
@RequiredArgsConstructor
public final class PkwareDecoder implements Decoder {

    private final PkwareEngine engine;

    public static PkwareDecoder create(@NonNull PathZipEntry entry, @NonNull DataInput in) throws IOException {
        PkwareEngine engine = new PkwareEngine(entry.getPassword());
        PkwareHeader.read(engine, entry, in);
        return new PkwareDecoder(engine);
    }

    @Override
    public void decrypt(@NonNull byte[] buf, int offs, int len) {
        engine.decrypt(buf, offs, len);
    }

    @Override
    public long getCompressedSize(@NonNull PathZipEntry entry) {
        return entry.getCompressedSize() - PkwareHeader.SIZE;
    }

}
