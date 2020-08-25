package ru.olegcherednik.zip4jvm.io.in.file;

import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.in.data.SingleZipInputStream;
import ru.olegcherednik.zip4jvm.io.out.data.SplitZipOutputStream;
import ru.olegcherednik.zip4jvm.model.ZipModel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 25.08.2020
 */
class StandardSplitSrcFile extends StandardSrcFile {

    static StandardSplitSrcFile create(Path file) {
        if (!Files.isReadable(file))
            return null;

        try (DataInput in = new SingleZipInputStream(new StandardSrcFile(ZipModel.getDiskFile(file, 1)))) {
            if (in.readDwordSignature() != SplitZipOutputStream.SPLIT_SIGNATURE)
                return null;
            return new StandardSplitSrcFile(file);
        } catch(IOException e) {
            return null;
        }
    }

    public StandardSplitSrcFile(Path path) {
        super(path);
    }
}
