package com.cop.zip4j.crypto.pkware;

import com.cop.zip4j.crypto.Encoder;
import com.cop.zip4j.exception.Zip4jException;
import com.cop.zip4j.io.out.DataOutput;
import com.cop.zip4j.model.Encryption;
import com.cop.zip4j.model.LocalFileHeader;
import com.cop.zip4j.model.entry.PathZipEntry;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.ArrayUtils;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 22.03.2019
 */
@RequiredArgsConstructor
public class PkwareEncoder implements Encoder {

    private final PkwareEngine engine;
    private final PkwareHeader header;

    public static PkwareEncoder create(@NonNull LocalFileHeader localFileHeader, @NonNull PathZipEntry entry) {
        if (ArrayUtils.isEmpty(entry.getPassword()))
            throw new Zip4jException("Passwords should not be empty for '" + Encryption.PKWARE.name() + "' encryption");

        PkwareEngine engine = new PkwareEngine(entry.getPassword());
        PkwareHeader header = PkwareHeader.create(localFileHeader, engine);
        return new PkwareEncoder(engine, header);
    }

    @Override
    public void encrypt(@NonNull byte[] buf, int offs, int len) {
        engine.encrypt(buf, offs, len);
    }

    @Override
    public void writeHeader(@NonNull DataOutput out) throws IOException {
        header.write(out);
    }

}
