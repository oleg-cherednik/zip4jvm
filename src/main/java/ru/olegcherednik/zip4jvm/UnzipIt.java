package ru.olegcherednik.zip4jvm;

import lombok.Builder;
import lombok.NonNull;
import ru.olegcherednik.zip4jvm.engine.UnzipEngine;
import ru.olegcherednik.zip4jvm.exception.Zip4jException;
import ru.olegcherednik.zip4jvm.io.readers.ZipModelReader;
import ru.olegcherednik.zip4jvm.model.ZipModel;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 14.03.2019
 */
@Builder
public class UnzipIt {

    @NonNull
    private final Path zipFile;
    private final char[] password;

    public InputStream extract(@NonNull String entryName) throws IOException {
        ZipModel zipModel = new ZipModelReader(zipFile).read();
        zipModel.getEntries().forEach(entry -> entry.setPassword(password));
        return new UnzipEngine(zipModel, password).extractEntry(entryName);
    }

    static void checkZipFile(Path zipFile) {
        if (!Files.exists(zipFile))
            throw new Zip4jException("ZipFile not exists: " + zipFile);
        if (!Files.isRegularFile(zipFile))
            throw new Zip4jException("ZipFile is not a regular file: " + zipFile);
    }

}
