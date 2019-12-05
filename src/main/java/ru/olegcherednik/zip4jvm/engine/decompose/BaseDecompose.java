package ru.olegcherednik.zip4jvm.engine.decompose;

import org.apache.commons.io.IOUtils;
import ru.olegcherednik.zip4jvm.model.block.Block;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 06.12.2019
 */
abstract class BaseDecompose {

    protected static void copyLarge(Path in, Path out, Block block) throws IOException {
        copyLarge(in, out, block.getOffs(), block.getSize());
    }

    protected static void copyLarge(Path in, Path out, long offs, long size) throws IOException {
        try (FileInputStream fis = new FileInputStream(in.toFile()); FileOutputStream fos = new FileOutputStream(out.toFile())) {
            copyLarge(fis, fos, offs, size);
        }
    }

    protected static void copyLarge(FileInputStream in, FileOutputStream out, long offs, long size) throws IOException {
        in.skip(offs);
        IOUtils.copyLarge(in, out, 0, size);
    }

}
