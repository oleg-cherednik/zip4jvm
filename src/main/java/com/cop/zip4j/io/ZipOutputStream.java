package com.cop.zip4j.io;

import com.cop.zip4j.core.writers.ZipModelWriter;
import com.cop.zip4j.model.ZipModel;
import lombok.NonNull;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 22.03.2019
 */
public class ZipOutputStream implements AutoCloseable {

    @NonNull
    public final SplitOutputStream out;
    @NonNull
    public final ZipModel zipModel;

    public ZipOutputStream(@NonNull SplitOutputStream out, @NonNull ZipModel zipModel) throws IOException {
        this.out = out;
        this.zipModel = zipModel;
        out.seek(zipModel.getOffsCentralDirectory());
    }

    @Override
    public void close() throws IOException {
        zipModel.getEndCentralDirectory().setOffs(out.getOffs());
        new ZipModelWriter(zipModel).finalizeZipFile(out, true);
        out.close();
    }

}
