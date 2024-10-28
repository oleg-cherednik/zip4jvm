package ru.olegcherednik.zip4jvm.io.out.entry;

import ru.olegcherednik.zip4jvm.io.out.data.DataOutput;
import ru.olegcherednik.zip4jvm.io.out.data.EncoderDataOutput;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Oleg Cherednik
 * @since 28.10.2024
 */
@RequiredArgsConstructor
public class SequenceOutputStream extends OutputStream {

    private final OutputStream os;

    public void writeBeforePayload(ZipEntry zipEntry, DataOutput out, EncoderDataOutput encoderDataOutput)
            throws IOException {
        encoderDataOutput.writeEncryptionHeader();
    }

    public void writePayload(InputStream in) throws IOException {
        IOUtils.copyLarge(in, this);
    }

    public void writeAfterPayload() {

    }

    @Override
    public void write(int b) throws IOException {
        os.write(b);
    }

    @Override
    public void write(byte[] buf, int offs, int len) throws IOException {
        os.write(buf, offs, len);
    }

    @Override
    public void close() throws IOException {
        os.close();
    }
}
