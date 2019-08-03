package com.cop.zip4j.io;

import com.cop.zip4j.core.writers.ZipModelWriter;
import com.cop.zip4j.model.ZipModel;
import lombok.NonNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;

/**
 * This class is responsible for write and correctly close ons single zip file or single part of split zip file.
 *
 * @author Oleg Cherednik
 * @since 08.03.2019
 */
public class SingleZipFileOutputStream extends MarkDataOutputStream {

    @NonNull
    private final ZipModel zipModel;

    @NonNull
    public static SingleZipFileOutputStream create(@NonNull ZipModel zipModel) throws FileNotFoundException {
        return new SingleZipFileOutputStream(zipModel.getZipFile(), zipModel);
    }

    @NonNull
    public static SingleZipFileOutputStream create(@NonNull Path zipFile, @NonNull ZipModel zipModel) throws FileNotFoundException {
        return new SingleZipFileOutputStream(zipFile, zipModel);
    }

    private SingleZipFileOutputStream(@NonNull Path zipFile, @NonNull ZipModel zipModel) throws FileNotFoundException {
        super(new LittleEndianWriteFile(zipFile));
        this.zipModel = zipModel;
    }

    @Override
    public void close() throws IOException {
        zipModel.getEndCentralDirectory().setOffs(getOffs());
        new ZipModelWriter(zipModel).finalizeZipFile(this, true);
        super.close();
    }

    @Override
    public int getCounter() {
        return 0;
    }

}
