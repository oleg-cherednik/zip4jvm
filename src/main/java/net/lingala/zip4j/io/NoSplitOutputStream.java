package net.lingala.zip4j.io;

import net.lingala.zip4j.model.ZipModel;

import java.io.FileNotFoundException;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 08.03.2019
 */
public class NoSplitOutputStream extends SplitOutputStream {

    public NoSplitOutputStream(Path file) throws FileNotFoundException {
        super(file, ZipModel.NO_SPLIT);
    }

}
