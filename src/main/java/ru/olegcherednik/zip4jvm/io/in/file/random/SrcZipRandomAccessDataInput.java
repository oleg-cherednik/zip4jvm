package ru.olegcherednik.zip4jvm.io.in.file.random;

import ru.olegcherednik.zip4jvm.model.src.SrcZip;

import lombok.Getter;

/**
 * The abstraction of random access regular file (or file). Source file details
 * can be read in the given {@code srcZip}. {@link SrcZip} means data on disc.
 *
 * @author Oleg Cherednik
 * @since 07.10.2026
 */
@Getter
public abstract class SrcZipRandomAccessDataInput extends BaseRandomAccessDataInput {

    protected final SrcZip srcZip;

    protected SrcZipRandomAccessDataInput(SrcZip srcZip) {
        super(srcZip.getSize(), srcZip.getByteOrder());
        this.srcZip = srcZip;
    }

}
