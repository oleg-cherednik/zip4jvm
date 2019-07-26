package com.cop.zip4j.io.delegate;

import com.cop.zip4j.model.ZipParameters;
import com.cop.zip4j.model.entry.PathZipEntry;
import lombok.NonNull;

import java.io.OutputStream;

/**
 * @author Oleg Cherednik
 * @since 26.07.2019
 */
public abstract class EntryOutputDelegate extends OutputStream {

    public abstract void putNextEntry(@NonNull PathZipEntry entry, @NonNull ZipParameters parameters);

}
