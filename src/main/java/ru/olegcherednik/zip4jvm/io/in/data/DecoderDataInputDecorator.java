package ru.olegcherednik.zip4jvm.io.in.data;

import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import ru.olegcherednik.zip4jvm.crypto.Decoder;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 07.02.2020
 */
@RequiredArgsConstructor
public final class DecoderDataInputDecorator extends BaseDataInput implements DecoderDataInput {

    private final DataInput delegate;
    private final Decoder decoder;

    @Override
    public long getDataCompressedSize(long compressedSize) {
        return decoder.getDataCompressedSize(compressedSize);
    }

    @Override
    public void decodingAccomplished() throws IOException {
        decoder.close(delegate);
    }

    @Override
    public long getAbsoluteOffs() {
        return delegate.getAbsoluteOffs();
    }

    @Override
    public long convertToAbsoluteOffs(int diskNo, long relativeOffs) {
        return delegate.convertToAbsoluteOffs(diskNo, relativeOffs);
    }

    @Override
    public long getDiskRelativeOffs() {
        return delegate.getDiskRelativeOffs();
    }

    @Override
    public SrcZip getSrcZip() {
        return delegate.getSrcZip();
    }

    @Override
    public SrcZip.Disk getDisk() {
        return delegate.getDisk();
    }

    @Override
    public long size() throws IOException {
        return delegate.getAbsoluteOffs();
    }

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        len = delegate.read(buf, offs, len);

        if (len != IOUtils.EOF)
            decoder.decrypt(buf, offs, len);

        return len;
    }

    @Override
    public long toLong(byte[] buf, int offs, int len) {
        return delegate.toLong(buf, offs, len);
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    @Override
    public long skip(long bytes) throws IOException {
        int total = 0;

        for (long i = 0; i < bytes; i++)
            total += readByte();

        return total;
    }

    @Override
    public void seek(long absoluteOffs) throws IOException {
        delegate.seek(absoluteOffs);
    }

    @Override
    public void seek(int diskNo, long relativeOffs) throws IOException {
        delegate.seek(diskNo, relativeOffs);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

}
