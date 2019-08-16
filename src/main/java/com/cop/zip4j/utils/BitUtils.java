package com.cop.zip4j.utils;

import lombok.experimental.UtilityClass;

/**
 * @author Oleg Cherednik
 * @since 06.03.2019
 */
@UtilityClass
public class BitUtils {

    public static final int BIT0 = 0b00000000_00000001;
    public static final int BIT1 = 0b00000000_00000010;
    public static final int BIT2 = 0b00000000_00000100;
    public static final int BIT3 = 0b00000000_00001000;
    public static final int BIT4 = 0b00000000_00010000;
    public static final int BIT5 = 0b00000000_00100000;
    public static final int BIT6 = 0b00000000_01000000;
    public static final int BIT7 = 0b00000000_10000000;

    public static final int BIT8 = 0b00000001_00000000;
    public static final int BIT9 = 0b00000010_00000000;
    public static final int BIT10 = 0b00000100_00000000;
    public static final int BIT11 = 0b00001000_00000000;
    public static final int BIT12 = 0b00010000_00000000;
    public static final int BIT13 = 0b00100000_00000000;
    public static final int BIT14 = 0b01000000_00000000;
    public static final int BIT15 = 0b10000000_00000000;

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

    public short setBits(short val, int bits) {
        return (short)setBits((int)val, bits);
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

    public static short clearBits(short val, int bits) {
        return (short)clearBits((int)val, bits);
    }

    public int updateBits(int val, int bits, boolean value) {
        return value ? setBits(val, bits) : clearBits(val, bits);
    }

    public short updateBits(short val, int bits, boolean value) {
        return (short)updateBits((int)val, bits, value);
    }
}
