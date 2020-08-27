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
        List<Item> items = createItems(file);
        return new StandardSplitSrcFile(file, items);
    }

    private static List<Item> createItems(Path file) {
        int i = 1;
        long offs = 0;
        List<Item> items = new LinkedList<>();
        Path dir = file.getParent();
        String baseName = FilenameUtils.getBaseName(file.getFileName().toString());

        for (Path path : getParts(dir, baseName + "\\.z\\d+")) {
            String actualFileName = path.getFileName().toString();
            String expectedFileName = String.format("%s.z%02d", baseName, i);

            if (!actualFileName.equals(expectedFileName) || !Files.isReadable(path))
                throw new SplitPartNotFoundException(dir.resolve(expectedFileName));

            long length = PathUtils.length(path);
            items.add(Item.builder()
                          .pos(i)
                          .file(path)
                          .offs(offs)
                          .length(length).build());
            offs += length;
            i++;
        }

        if (i == getTotalDisks(file))
            throw new SplitPartNotFoundException(dir.resolve(String.format("%s.%02d", baseName, i)));

        items.add(Item.builder()
                      .pos(i)
                      .file(file)
                      .offs(offs)
                      .length(PathUtils.length(file)).build());

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
        return new StandardSplitLittleEndianReadFile(this);
    }

}
