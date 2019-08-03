package com.cop.zip4j.io;

import com.cop.zip4j.core.writers.ZipModelWriter;
import com.cop.zip4j.model.ZipModel;
import lombok.NonNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Oleg Cherednik
 * @since 08.03.2019
 */
public class SingleZipFileOutputStream extends DataOutputStreamAdapter {

    @NonNull
    private final ZipModel zipModel;

    @NonNull
    public static SingleZipFileOutputStream create(@NonNull ZipModel zipModel) throws IOException {
        Path zipFile = zipModel.getZipFile();
        Path parent = zipFile.getParent();

        if (parent != null)
            Files.createDirectories(parent);

        SingleZipFileOutputStream out = new SingleZipFileOutputStream(zipFile, zipModel);
        out.seek(zipModel.getOffsCentralDirectory());
        return out;
    }

    public SingleZipFileOutputStream(@NonNull Path zipFile, @NonNull ZipModel zipModel) throws FileNotFoundException {
        super(new LittleEndianWriteFile(zipFile));
        this.zipModel = zipModel;
    }

    @Override
    public void close() throws IOException {
        zipModel.getEndCentralDirectory().setOffs(getOffs());
        new ZipModelWriter(zipModel).finalizeZipFile(out, true);
        super.close();
    }

    @Override
    public int getCurrSplitFileCounter() {
        return 0;
    }

    // -------------------

    private final Map<String, Long> mark = new HashMap<>();

    @Override
    public void mark(String id) {
        mark.put(id, getOffs());
    }

    @Override
    public long getWrittenBytesAmount(String id) {
        return getOffs() - mark.getOrDefault(id, 0L);
    }


}
