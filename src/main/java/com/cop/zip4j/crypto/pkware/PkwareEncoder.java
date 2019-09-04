package com.cop.zip4j.crypto.pkware;

import com.cop.zip4j.crypto.Encoder;
import com.cop.zip4j.io.out.DataOutput;
import com.cop.zip4j.model.entry.ZipEntry;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 22.03.2019
 */
@RequiredArgsConstructor
public final class PkwareEncoder implements Encoder {

    private final PkwareEngine engine;
    private final PkwareHeader header;

    public static PkwareEncoder create(@NonNull ZipEntry entry) {
        PkwareEngine engine = new PkwareEngine(entry.getPassword());
        PkwareHeader header = PkwareHeader.create(engine, entry.getLastModifiedTime());
        return new PkwareEncoder(engine, header);
    }

    @Override
    public void writeEncryptionHeader(@NonNull DataOutput out) throws IOException {
        header.write(out);
    }

    @Override
    public void encrypt(@NonNull byte[] buf, int offs, int len) {
        engine.encrypt(buf, offs, len);
    }

}
