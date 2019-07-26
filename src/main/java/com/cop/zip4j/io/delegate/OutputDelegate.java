package com.cop.zip4j.io.delegate;

import com.cop.zip4j.model.ZipParameters;
import com.cop.zip4j.model.entry.PathZipEntry;
import lombok.NonNull;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 26.07.2019
 */
public interface OutputDelegate {

    void putNextEntry(@NonNull PathZipEntry entry, @NonNull ZipParameters parameters);

    void write(byte[] buf, int offs, int len) throws IOException;

    void closeEntry() throws IOException;

}
