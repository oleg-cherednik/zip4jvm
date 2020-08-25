package ru.olegcherednik.zip4jvm.io.in.file;

import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.in.data.SingleZipInputStream;
import ru.olegcherednik.zip4jvm.io.readers.BaseZipModelReader;
import ru.olegcherednik.zip4jvm.io.readers.EndCentralDirectoryReader;
import ru.olegcherednik.zip4jvm.model.Charsets;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

/**
 * @author Oleg Cherednik
 * @since 25.08.2020
 */
class StandardSplitSrcFile extends SrcFile {

    public static boolean isCandidate(Path file) {
        return Files.isReadable(file) && getTotalDisks(file) > 0;
    }

    public static StandardSplitSrcFile create(Path file) {



        return new StandardSplitSrcFile(file);
    }

    private static int getTotalDisks(Path file) {
        try (DataInput in = new SingleZipInputStream(StandardSolidSrcFile.create(file))) {
            BaseZipModelReader.findCentralDirectorySignature(in);
            return new EndCentralDirectoryReader(Charsets.UNMODIFIED).read(in).getTotalDisks();
        } catch(Exception e) {
            return 0;
        }
    }

    private StandardSplitSrcFile(Path path) {
        super(path, Collections.singletonList(Item.create(path)));
    }

    @Override
    public DataInputFile dataInputFile() throws IOException {
        return new LittleEndianReadFile(path);
    }

    @Override
    public boolean isSplit() {
        return true;
    }
}
