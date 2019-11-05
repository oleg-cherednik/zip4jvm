package ru.olegcherednik.zip4jvm.io.in.ng;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 27.10.2019
 */
public class SingleZipInputStream extends BaseZipInputStream {

    public SingleZipInputStream(Path zip) throws FileNotFoundException {
        super(new FileInputStream(zip.toFile()));
    }

}
