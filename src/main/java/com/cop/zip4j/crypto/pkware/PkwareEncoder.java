package com.cop.zip4j.crypto.pkware;

import com.cop.zip4j.crypto.Encoder;
import com.cop.zip4j.io.out.DataOutput;
import com.cop.zip4j.model.entry.PathZipEntry;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 22.03.2019
 */
@RequiredArgsConstructor
public class PkwareEncoder implements Encoder {

    private final PkwareEngine engine;
    private final PkwareHeader header;

    public static PkwareEncoder create(@NonNull PathZipEntry entry) {
        PkwareEngine engine = new PkwareEngine(entry.getPassword());
        PkwareHeader header = PkwareHeader.create(engine, entry.getLastModifiedTime());
        return new PkwareEncoder(engine, header);
    }

    @Override
    public void writeHeader(@NonNull DataOutput out) throws IOException {
        header.write(out);
    }

    @Override
    public void encrypt(@NonNull byte[] buf, int offs, int len) {
        engine.encrypt(buf, offs, len);
    }

}
