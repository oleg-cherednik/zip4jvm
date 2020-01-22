package ru.olegcherednik.zip4jvm.io.in;

import lombok.Getter;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 12.12.2019
 */
@Getter
public abstract class BaseZipDataInput extends BaseDataInput implements ZipDataInput {

    protected final Zip zip;

    protected BaseZipDataInput(Zip zip) throws FileNotFoundException {
        this.zip = zip;
        delegate = new LittleEndianReadFile(zip.getDiskPath());
    }

    @Override
    public long getTotalDisks() {
        return zip.getTotalDisks();
    }

    public long length() throws IOException {

    }

}
