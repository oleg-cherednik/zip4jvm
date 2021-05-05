package ru.olegcherednik.zip4jvm.model;

import lombok.Getter;
import org.apache.commons.lang3.ArrayUtils;
import ru.olegcherednik.zip4jvm.utils.BitUtils;

import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT0;

/**
 * @author Oleg Cherednik
 * @since 16.08.2019
 */
@SuppressWarnings("MethodCanBeVariableArityMethod")
public final class InternalFileAttributes {

    public static final int SIZE = 2;

    @Getter
    private ApparentFileType apparentFileType = ApparentFileType.BINARY;

    private final byte[] data = new byte[SIZE];

    public static InternalFileAttributes build(byte[] data) {
        return new InternalFileAttributes().readFrom(data);
    }

    public InternalFileAttributes readFrom(InternalFileAttributes internalFileAttributes) {
        return readFrom(internalFileAttributes.data);
    }

    private InternalFileAttributes readFrom(byte[] data) {
        System.arraycopy(data, 0, this.data, 0, SIZE);
        apparentFileType = BitUtils.isBitSet(data[0], BIT0) ? ApparentFileType.TEXT : ApparentFileType.BINARY;
        return this;
    }

    public byte[] getData() {
        byte[] data = ArrayUtils.clone(this.data);
        data[0] = BitUtils.updateBits((byte)0x0, BIT0, apparentFileType == ApparentFileType.TEXT);
        return data;
    }

    @Override
    public String toString() {
        return "internal";
    }

}
