package ru.olegcherednik.zip4jvm.model.src;

import lombok.Getter;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.math.NumberUtils;
import ru.olegcherednik.zip4jvm.exception.SplitPartNotFoundException;
import ru.olegcherednik.zip4jvm.io.in.file.DataInputFile;
import ru.olegcherednik.zip4jvm.io.in.file.SrcFileLittleEndianReadFile;
import ru.olegcherednik.zip4jvm.utils.PathUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * 7-Zip has not standard split algorithm. It creates the whole zip file first and then split it with required part size. It has following naming
 * convention:
 * <pre>
 * filename.zip.001
 * filename.zip.002
 * filename.zip.003
 * </pre>
 * According to the zip specification, this is not a split archive.
 *
 * @author Oleg Cherednik
 * @since 20.01.2020
 */
@Getter
final class SevenZipSplitSrcFile extends SrcFile {

    public static boolean isCandidate(Path file) {
        String ext = FilenameUtils.getExtension(file.toString());
        return Files.isReadable(file) && NumberUtils.isDigits(ext);
    }

    static SevenZipSplitSrcFile create(Path file) {
        List<Item> items = createItems(file);
        return new SevenZipSplitSrcFile(file, items);
    }

    private static List<Item> createItems(Path file) {
        int i = 1;
        long offs = 0;
        List<Item> items = new LinkedList<>();
        Path dir = file.getParent();
        String baseName = FilenameUtils.getBaseName(file.getFileName().toString());

        for (Path path : getParts(dir, baseName + "\\.\\d+")) {
            String actualFileName = path.getFileName().toString();
            String expectedFileName = String.format("%s.%03d", baseName, i);

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

        return items.isEmpty() ? Collections.emptyList() : Collections.unmodifiableList(items);
    }

    private SevenZipSplitSrcFile(Path path, List<Item> items) {
        super(path, items);
    }

    @Override
    public boolean isSplit() {
        // TODO on this result we check split signature (this is valid only for standard split zip) - see ZipModel.createDataInput()
        return false;
    }

    @Override
    public DataInputFile dataInputFile() throws IOException {
        return new SrcFileLittleEndianReadFile(this);
    }

}
