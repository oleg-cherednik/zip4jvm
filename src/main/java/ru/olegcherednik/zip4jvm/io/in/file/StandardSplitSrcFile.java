package ru.olegcherednik.zip4jvm.io.in.file;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.math.NumberUtils;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.in.data.SingleZipInputStream;
import ru.olegcherednik.zip4jvm.io.out.data.SplitZipOutputStream;
import ru.olegcherednik.zip4jvm.model.ZipModel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

/**
 * @author Oleg Cherednik
 * @since 25.08.2020
 */
class StandardSplitSrcFile extends StandardSrcFile {

    public static boolean isCandidate(Path file) {
        if(!Files.isReadable(file))
            return false;

        Path file = ZipModel.getDiskFile(path, 1);

        try (DataInput in = new SingleZipInputStream(new StandardSplitSrcFile(file))) {

            return in.readDwordSignature() == SplitZipOutputStream.SPLIT_SIGNATURE;
        } catch(IOException e) {
            e.printStackTrace();
        }


        String fileName = file.getFileName().toString();
        String ext = FilenameUtils.getExtension(fileName);
        return Files.isReadable(file) && NumberUtils.isDigits(ext);
    }

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
