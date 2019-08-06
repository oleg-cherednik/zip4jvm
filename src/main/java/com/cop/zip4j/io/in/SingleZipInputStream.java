package com.cop.zip4j.io.in;

import com.cop.zip4j.model.ZipModel;
import lombok.NonNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 04.08.2019
 */
public class SingleZipInputStream extends BaseMarkDataInput {

    @NonNull
    public static SingleZipInputStream create(@NonNull ZipModel zipModel) throws FileNotFoundException {
        return new SingleZipInputStream(zipModel.getZipFile(), zipModel);
    }

    private SingleZipInputStream(@NonNull Path zipFile, @NonNull ZipModel zipModel) throws FileNotFoundException {
        super(zipModel);
        delegate = new LittleEndianReadFile(zipFile);
    }

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        return delegate.read(buf, offs, len);
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

}
