package ru.olegcherednik.zip4jvm.io.in.file;

import org.apache.commons.io.FilenameUtils;
import ru.olegcherednik.zip4jvm.exception.SplitPartNotFoundException;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.in.data.SingleZipInputStream;
import ru.olegcherednik.zip4jvm.io.readers.BaseZipModelReader;
import ru.olegcherednik.zip4jvm.io.readers.EndCentralDirectoryReader;
import ru.olegcherednik.zip4jvm.model.Charsets;
import ru.olegcherednik.zip4jvm.utils.PathUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Oleg Cherednik
 * @since 25.08.2020
 */
class StandardSplitSrcFile extends SrcFile {

    public static boolean isCandidate(Path file) {
        return Files.isReadable(file) && getTotalDisks(file) > 0;
    }

    public static StandardSplitSrcFile create(Path file) {
        String baseName = FilenameUtils.getBaseName(file.getFileName().toString());
        List<Item> items = createItems(file.getParent(), baseName);
        return new StandardSplitSrcFile(file, items);
    }

    private static List<Item> createItems(Path dir, String baseName) {
        int i = 1;
        long offs = 0;
        List<Item> items = new LinkedList<>();

        for (Path path : getParts(dir, baseName + "\\.z\\d+")) {
            String actualFileName = path.getFileName().toString();
            String expectedFileName = String.format("%s.z%02d", baseName, i);

            if (!actualFileName.equals(expectedFileName) || !Files.isReadable(path))
                throw new SplitPartNotFoundException(dir.resolve(expectedFileName));

            long length = PathUtils.length(path);
            items.add(Item.builder()
                          .file(path)
                          .offs(offs)
                          .length(length).build());
            offs += length;
            i++;
        }

        return items.isEmpty() ? Collections.emptyList() : Collections.unmodifiableList(items);
    }

    private static int getTotalDisks(Path file) {
        try (DataInput in = new SingleZipInputStream(StandardSolidSrcFile.create(file))) {
            BaseZipModelReader.findCentralDirectorySignature(in);
            return new EndCentralDirectoryReader(Charsets.UNMODIFIED).read(in).getTotalDisks();
        } catch(Exception e) {
            return 0;
        }
    }

    private StandardSplitSrcFile(Path path, List<Item> items) {
        super(path, items);
    }

    @Override
    public DataInputFile dataInputFile() throws IOException {
        return new LittleEndianReadFile(path);
    }

}
