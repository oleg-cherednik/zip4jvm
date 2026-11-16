package io.airlift.compress.zstd.seq;

import io.airlift.compress.zstd.In;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static io.airlift.compress.zstd.Constants.LONG_NUMBER_OF_SEQUENCES;

/**
 * see: 3.1.1.3.2.1.
 *
 * @author Oleg Cherednik
 * @since 16.11.2026
 */
@Getter
public class SequenceHeader {

    private int count;
    private CompressionMode literalsLengthCompressionMode;
    private CompressionMode offsetCompressionMode;
    private CompressionMode matchLengthCompressionMode;

    public static SequenceHeader decode(In in) {
        SequenceHeader sequenceHeader = new SequenceHeader();

        int b0 = in.readByte();

        if (b0 == 0)
            sequenceHeader.count = 0;
        else {
            if (b0 == 255)
                // 0 == 255: Number_of_Sequences = b1 + (b2 << 8) + 0x7F00 (3 bytes)
                sequenceHeader.count = in.readWord() + LONG_NUMBER_OF_SEQUENCES;
            else
                // b0 < 255: Number_of_Sequences = ((b0 - 128) << 8) + b1 (2 bytes)
                sequenceHeader.count = ((b0 - 128) << 8) + in.readByte();

            int b = in.readByte();

            sequenceHeader.literalsLengthCompressionMode = CompressionMode.parseId((b & 0xFF) >>> 6);
            sequenceHeader.offsetCompressionMode = CompressionMode.parseId((b >>> 4) & 0b11);
            sequenceHeader.matchLengthCompressionMode = CompressionMode.parseId((b >>> 2) & 0b11);
        }

        return sequenceHeader;
    }

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PACKAGE)
    public enum CompressionMode {
        /**
         * A predefined FSE (see Section 4.1) distribution table is used,
         * as defined in Section 3.1.1.3.2.2. No distribution table will be present.
         */
        PREDEFINED(0),
        /**
         * The table description consists of a single byte, which contains the
         * symbol's value. This symbol will be used for all sequences.
         */
        RLE(1),
        /**
         * Standard FSE compression. A distribution table will be present.
         * The format of this distribution table is described in Section 4.1.1.
         * Note that the maximum allowed accuracy log for literals length code and
         * match length code tables is 9, and the maximum accuracy log for the
         * offset code table is 8. This mode must not be used when only one symbol
         * is present; RLE_Mode should be used instead (although any other mode
         * will work).
         */
        FSE_COMPRESSED(2),
        /**
         * The table used in the previous Compressed_Block with
         * Number_Of_Sequences > 0 will be used again, or if this is the first
         * block, the table in the dictionary will be used. Note that this includes
         * RLE_Mode, so if Repeat_Mode follows RLE_Mode, the same symbol will be
         * repeated. It also includes Predefined_Mode, in which case Repeat_Mode
         * will have the same outcome as Predefined_Mode. No distribution table
         * will be present. If this mode is used without any previous sequence
         * table in the frame (or dictionary; see Section 5) to repeat, this
         * should be treated as corruption.
         */
        REPEAT(3);

        private final int id;

        public static CompressionMode parseId(int id) {
            for (CompressionMode compressionMode : values())
                if (compressionMode.id == id)
                    return compressionMode;

            throw new EnumConstantNotPresentException(CompressionMode.class, String.valueOf(id));
        }

    }

}
