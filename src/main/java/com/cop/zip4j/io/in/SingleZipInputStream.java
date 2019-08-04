package com.cop.zip4j.io.in;

import com.cop.zip4j.model.ZipModel;
import lombok.NonNull;

import java.io.FileNotFoundException;

/**
 * @author Oleg Cherednik
 * @since 04.08.2019
 */
public class SingleZipInputStream extends BaseDataInput {

    @NonNull
    public static SingleZipInputStream create(@NonNull ZipModel zipModel) throws FileNotFoundException {
        return new SingleZipInputStream(new LittleEndianReadFile(zipModel.getZipFile()));
    }

    private SingleZipInputStream(@NonNull DataInput delegate) {
        super(delegate);
    }

}
