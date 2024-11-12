package ru.olegcherednik.zip4jvm.io.in.data;

import ru.olegcherednik.zip4jvm.io.in.file.DataInputFile;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import lombok.Getter;

/**
 * @author Oleg Cherednik
 * @since 12.11.2024
 */
@Getter
public abstract class RandomAccessFileBaseDataInput extends BaseDataInput implements DataInputFile {

    protected final SrcZip srcZip;

    protected RandomAccessFileBaseDataInput(SrcZip srcZip) {
        super(srcZip.getByteOrder());
        this.srcZip = srcZip;
    }

    // ---------- DataInputFile ----------

    @Override
    public long convertToAbsoluteOffs(int diskNo, long relativeOffs) {
        return srcZip.getDiskByNo(diskNo).getAbsoluteOffs() + relativeOffs;
    }

    // ---------- DataInput ----------

    @Override
    public int byteSize() {
        return 1;
    }

    @Override
    public int wordSize() {
        return 2;
    }

    @Override
    public int dwordSize() {
        return 4;
    }

    @Override
    public int qwordSize() {
        return 8;
    }

    @Override
    public int readByte() {
        return Quietly.doQuietly(() -> byteOrder.readByte(this));
    }

    @Override
    public int readWord() {
        return Quietly.doQuietly(() -> byteOrder.readWord(this));
    }

    @Override
    public long readDword() {
        return Quietly.doQuietly(() -> byteOrder.readDword(this));
    }

    @Override
    public long readQword() {
        return Quietly.doQuietly(() -> byteOrder.readQword(this));
    }

}
