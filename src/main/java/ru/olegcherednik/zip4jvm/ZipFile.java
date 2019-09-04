package ru.olegcherednik.zip4jvm;

import ru.olegcherednik.zip4jvm.model.ZipParameters;
import lombok.NonNull;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;

/**
 * ZipFile real-time implementation.
 * <br>
 * When create new instance of this class:
 * <ul>
 * <li><i>zip file exists</i> - open zip archive</li>
 * <li><i>zip file not exists</i> - create new empty zip archive</li>
 * </ul>
 * <p>
 * To close zip archive correctly, do call {@link ZipFile#close()} method.
 * <pre>
 * try (ZipFile zipFile = new ZipFile(Paths.get("~/src.zip"))) {
 *     zipFile.addEntry(...);
 * }
 * </pre>
 *
 * @author Oleg Cherednik
 * @since 01.09.2019
 */
public final class ZipFile implements Closeable {

    public ZipFile(@NonNull Path zip) {
        this(zip, ZipParameters.builder().build());
    }

    public ZipFile(@NonNull Path zip, @NonNull ZipParameters parameters) {

    }

    @Override
    public void close() throws IOException {

    }
}
