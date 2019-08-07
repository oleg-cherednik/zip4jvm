package com.cop.zip4j.io.out;

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
public class SingleZipOutputStream extends BaseMarkDataOutput {

    @NonNull
    public static SingleZipOutputStream create(@NonNull ZipModel zipModel) throws FileNotFoundException {
        return new SingleZipOutputStream(zipModel.getZipFile(), zipModel);
    }

    @NonNull
    public static SingleZipOutputStream create(@NonNull Path zipFile, @NonNull ZipModel zipModel) throws FileNotFoundException {
        return new SingleZipOutputStream(zipFile, zipModel);
    }

    private SingleZipOutputStream(Path zipFile, ZipModel zipModel) throws FileNotFoundException {
        super(zipModel);
        createNewFile(zipFile);
    }

    @Override
    public int getCounter() {
        return 0;
    }

    @Override
    public void close() throws IOException {
        zipModel.getEndCentralDirectory().setOffs(getOffs());
        new ZipModelWriter(zipModel).finalizeZipFile(this, true);
        super.close();
    }

}
