package ru.olegcherednik.zip4jvm.io.in;

import lombok.NonNull;
import ru.olegcherednik.zip4jvm.model.ZipModel;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 04.08.2019
 */
public class SingleZipInputStream extends BaseDataInput {

    @NonNull
    public static SingleZipInputStream create(@NonNull ZipModel zipModel) throws FileNotFoundException {
        return new SingleZipInputStream(zipModel.getFile());
    }

    private SingleZipInputStream(Path zip) throws FileNotFoundException {
        delegate = new LittleEndianReadFile(zip);
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
