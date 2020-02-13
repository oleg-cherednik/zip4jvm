/*
 * RangeDecoderFromStream
 *
 * Authors: Lasse Collin <lasse.collin@tukaani.org>
 *          Igor Pavlov <http://7-zip.org/>
 *
 * This file has been put into the public domain.
 * You can do whatever you want with this file.
 */

package ru.olegcherednik.zip4jvm.io.lzma.xz.rangecoder;

import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.lzma.xz.exceptions.CorruptedInputException;

import java.io.IOException;

public final class RangeDecoderFromStream extends RangeDecoder {

    private final DataInput in;

    public RangeDecoderFromStream(DataInput in) throws IOException {
        this.in = in;
        if (this.in.readByte() != 0x00)
            throw new CorruptedInputException();

        for (int i = 0; i < 4; ++i)
            code = code << 8 | in.readByte();

        range = 0xFFFFFFFF;
    }

    public void normalize() throws IOException {
        if ((range & TOP_MASK) == 0) {
            code = (code << SHIFT_BITS) | in.readByte();
            range <<= SHIFT_BITS;
        }
    }
}
