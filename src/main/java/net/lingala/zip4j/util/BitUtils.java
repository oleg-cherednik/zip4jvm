package net.lingala.zip4j.util;

import lombok.experimental.UtilityClass;

/**
 * @author Oleg Cherednik
 * @since 06.03.2019
 */
@UtilityClass
public class BitUtils {
    public static final int BIT0 = 0x1;
    public static final int BIT1 = 0x2;
    public static final int BIT2 = 0x4;
    public static final int BIT3 = 0x8;
    public static final int BIT4 = 0x10;
    public static final int BIT5 = 0x20;
    public static final int BIT6 = 0x40;
    public static final int BIT7 = 0x80;

    public long getByte(long val, int i) {
        return (val >> i * 8) & 0xFF;
    }

    /**
     * Checks if all bits of giving bit set are set or not
     *
     * @param val  checked val
     * @param bits checked bit or bit set
     * @return {@literal true} if all selected bit(s) are set
     */
    public boolean isBitSet(int val, int bits) {
        return (val & bits) == bits;
    }

    /**
     * Set selected bit(s) in giving val
     *
     * @param val  val
     * @param bits bit or bit set to set in the val
     * @return {@literal val} with set selected bits
     */
    public int setBits(int val, int bits) {
        return val | bits;
    }

    /**
     * Clear selected bit(s) in giving val
     *
     * @param val  val
     * @param bits bit or bit set to clear in the val
     * @return {@literal val} with cleared selected bits
     */
    public static int clearBits(int val, int bits) {
        return val & ~bits;
    }
}
