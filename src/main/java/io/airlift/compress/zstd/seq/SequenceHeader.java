package io.airlift.compress.zstd.seq;

import io.airlift.compress.zstd.In;
import io.airlift.compress.zstd.OutByteArrayWithOffs;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * see: 3.1.1.3.2.1.
 *
 * @author Oleg Cherednik
 * @since 16.11.2026
 */
@Getter
@Setter
public class SequenceHeader {

    private static final int SEQ_1_BYTE = 127;
    private static final int LONG_NUMBER_OF_SEQUENCES = 0x7F00;

    private int sequenceCount;
    private CompressionMode literalsLengthCompressionMode;
    private CompressionMode offsetCompressionMode;
    private CompressionMode matchLengthCompressionMode;

    public static SequenceHeader decode(In in) {
        int b0 = in.readByte();

        SequenceHeader sequenceHeader = new SequenceHeader();
        sequenceHeader.sequenceCount = b0;

        if (b0 != 0) {
            if (b0 == 255)
                sequenceHeader.sequenceCount = in.readWord() + LONG_NUMBER_OF_SEQUENCES;
            else if (b0 > 127)
                sequenceHeader.sequenceCount = ((b0 - 128) << 8) + in.readByte();

            int b = in.readByte();

            sequenceHeader.literalsLengthCompressionMode = CompressionMode.parseId((b & 0xFF) >>> 6);
            sequenceHeader.offsetCompressionMode = CompressionMode.parseId((b >>> 4) & 0b11);
            sequenceHeader.matchLengthCompressionMode = CompressionMode.parseId((b >>> 2) & 0b11);
        }

        return sequenceHeader;
    }

    public void encodeSequenceCount(OutByteArrayWithOffs out) {
        if (sequenceCount <= SEQ_1_BYTE) {
            System.out.println("1 byte");
            out.putByte((byte) sequenceCount);
        } else if (sequenceCount <= LONG_NUMBER_OF_SEQUENCES) {
            System.out.println("2 bytes");
            out.putByte((byte) (sequenceCount >>> 8 | 0x80));
            out.putByte((byte) sequenceCount);
        } else {
            System.out.println("3 bytes");
            out.putByte((byte) 0xFF);
            out.putShort((short) (sequenceCount - LONG_NUMBER_OF_SEQUENCES));
        }
    }

    public void encodeCompressionMode(OutByteArrayWithOffs out) {
        if (literalsLengthCompressionMode != null
                && offsetCompressionMode != null
                && matchLengthCompressionMode != null) {
            out.putByte((byte) ((literalsLengthCompressionMode.id << 6)
                    | (offsetCompressionMode.id << 4)
                    | (matchLengthCompressionMode.id << 2)));
        }
    }

    public int getHeaderSize() {
        int size = sequenceCount > 0 ? 1 : 0;

        if (sequenceCount <= SEQ_1_BYTE)
            return size + 1;
        if (sequenceCount <= LONG_NUMBER_OF_SEQUENCES)
            return size + 2;

        return size + 3;
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
