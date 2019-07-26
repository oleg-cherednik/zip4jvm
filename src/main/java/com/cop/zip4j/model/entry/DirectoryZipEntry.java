package com.cop.zip4j.model.entry;

import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 26.07.2019
 */
public class DirectoryZipEntry extends PathZipEntry {

    public DirectoryZipEntry(Path dir) {
        super(dir);
    }

}
