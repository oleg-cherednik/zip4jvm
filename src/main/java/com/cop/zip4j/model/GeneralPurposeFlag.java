package com.cop.zip4j.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import com.cop.zip4j.utils.BitUtils;

import static com.cop.zip4j.utils.BitUtils.BIT0;
import static com.cop.zip4j.utils.BitUtils.BIT1;
import static com.cop.zip4j.utils.BitUtils.BIT11;
import static com.cop.zip4j.utils.BitUtils.BIT2;
import static com.cop.zip4j.utils.BitUtils.BIT3;
import static com.cop.zip4j.utils.BitUtils.BIT6;

/**
 * @author Oleg Cherednik
 * @since 12.03.2019
 */
@Getter
@Setter
@AllArgsConstructor
public final class GeneralPurposeFlag {

    /*
     * Bit 0: If set, indicates that the file is encrypted.
     *
     * (For Methods 8 and 9 - Deflating)
     * Bit 2  Bit 1
     *   0      0    Normal (-en) compression option was used.
     *   0      1    Maximum (-exx/-ex) compression option was used.
     *   1      0    Fast (-ef) compression option was used.
     *   1      1    Super Fast (-es) compression option was used.
     *
     * Bit 3: If this bit is set, the fields crc-32, compressed
     *        size and uncompressed size are set to zero in the
     *        local header.  The correct values are put in the
     *        data descriptor immediately following the compressed
     *        data.  (Note: PKZIP version 2.04g for DOS only
     *        recognizes this bit for method 8 compression, newer
     *        versions of PKZIP recognize this bit for any
     *        compression method.)
     *
     * Bit 6: Strong encryption.  If this bit is set, you MUST
     *        set the version needed to extractEntries value to at least
     *        50 and you MUST also set bit 0.  If AES encryption
     *        is used, the version needed to extractEntries value MUST
     *        be at least 51. See the section describing the Strong
     *        Encryption Specification for details.  Refer to the
     *        section in this document entitled "Incorporating PKWARE
     *        Proprietary Technology into Your Product" for more
     *        information.
     *
     * Bit 11: Language encoding flag (EFS).  If this bit is set,
     *         the filename and comment fields for this file
     *         MUST be encoded using UTF-8.
     */
    private int data;

    public GeneralPurposeFlag() {
        this((short)0);
    }

    public boolean isEncrypted() {
        return BitUtils.isBitSet(data, BIT0);
    }

    public void setEncrypted(boolean val) {
        data = BitUtils.updateBits(data, BIT0, val);
    }

    public boolean isDataDescriptorExists() {
        return BitUtils.isBitSet(data, BIT3);
    }

    public void setDataDescriptorExists(boolean val) {
        data = BitUtils.updateBits(data, BIT3, val);
    }

    public boolean isStrongEncryption() {
        return BitUtils.isBitSet(data, BIT6);
    }

    public void setStrongEncryption(boolean val) {
        data = BitUtils.updateBits(data, BIT6, val);
    }

    public boolean isUtf8Encoding() {
        return BitUtils.isBitSet(data, BIT11);
    }

    public void setUtf8Encoding(boolean val) {
        data = BitUtils.updateBits(data, BIT11, val);
    }

    public void setCompressionLevel(@NonNull CompressionLevel compressionLevel) {
        data = BitUtils.clearBits(data, BIT1 & BIT2);

        if (compressionLevel == CompressionLevel.MAXIMUM)
            data = BitUtils.setBits(data, BIT1);
        else if (compressionLevel == CompressionLevel.FAST)
            data = BitUtils.setBits(data, BIT2);
        else if (compressionLevel == CompressionLevel.FASTEST)
            data = BitUtils.setBits(data, BIT1 | BIT2);
    }

}
