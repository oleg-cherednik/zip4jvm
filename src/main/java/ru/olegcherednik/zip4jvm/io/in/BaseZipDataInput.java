package ru.olegcherednik.zip4jvm.io.in;

import lombok.Getter;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 12.12.2019
 */
@Getter
public abstract class BaseZipDataInput extends BaseDataInput implements ZipDataInput {

    protected final Zip zip;

    protected BaseZipDataInput(Zip zip) throws IOException {
        this.zip = zip;
        delegate = zip instanceof MultipleZip ? new SevenLittleEndianReadFile((MultipleZip)zip)
                                              : new LittleEndianReadFile(zip.getDiskPath(0));
    }

    @Override
    public long getTotalDisks() {
        return zip.getTotalDisks();
    }

}
