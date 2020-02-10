package ru.olegcherednik.zip4jvm.io.lzma.xz.rangecoder;

import ru.olegcherednik.zip4jvm.io.out.DataOutput;

import java.io.IOException;

public final class RangeEncoderToStream extends RangeEncoder {
    private final DataOutput out;

    public RangeEncoderToStream(DataOutput out) {
        this.out = out;
        reset();
    }

    void writeByte(int b) throws IOException {
        out.writeByte(b);
    }
}
