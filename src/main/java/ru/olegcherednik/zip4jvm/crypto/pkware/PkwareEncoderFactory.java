package ru.olegcherednik.zip4jvm.crypto.pkware;

import ru.olegcherednik.zip4jvm.crypto.Encoder;
import ru.olegcherednik.zip4jvm.crypto.EncoderFactory;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireNotEmpty;

/**
 * @author Oleg Cherednik
 * @since 01.08.2026
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PkwareEncoderFactory implements EncoderFactory {

    public static final PkwareEncoderFactory INSTANCE = new PkwareEncoderFactory();

    // ---------- EncoderFactory ----------

    @Override
    public Encoder createEncoder(ZipEntry zipEntry) {
        requireNotEmpty(zipEntry.getPassword(), zipEntry.getFileName() + ".password");

        PkwareEngine engine = new PkwareEngine(zipEntry.getPassword());
        int key = zipEntry.isDataDescriptorAvailable() ? zipEntry.getLastModifiedTime()
                                                       : (int) zipEntry.getCrc32() >> 16;

        return new PkwareEncoder(engine, PkwareHeader.create(engine, key));
    }
}
