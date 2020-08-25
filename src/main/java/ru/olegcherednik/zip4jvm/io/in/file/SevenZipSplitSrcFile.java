package ru.olegcherednik.zip4jvm.io.in.file;

import lombok.Getter;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.math.NumberUtils;
import ru.olegcherednik.zip4jvm.exception.SplitPartNotFoundException;
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
        if(!Files.isReadable(file))
            return null;

        String fileName = file.getFileName().toString();
        String ext = FilenameUtils.getExtension(fileName);
        Path parent = file.getParent();

        if (!"zip".equalsIgnoreCase(ext) && NumberUtils.isDigits(ext))
            fileName = FilenameUtils.getBaseName(fileName);
        if (!Files.exists(parent.resolve(fileName + ".001")))
            return null;

        long offs = 0;
        List<Item> items = createItems(file, );

        for (int i = 0; ; i++) {
            Path path = parent.resolve(String.format("%s.%03d", fileName, i + 1));

            if (!Files.exists(path))
                break;

            long length = PathUtils.length(path);
            items.add(Item.builder().pos(i).file(path).offs(offs).length(length).build());
            offs += length;
        }

        return new SevenZipSplitSrcFile(parent.resolve(fileName), Collections.unmodifiableList(items), offs);
    }

    private static List<Item> createItems(Path dir, String baseName) {
        int i = 1;
        long offs = 0;
        List<Item> items = new LinkedList<>();

        for (Path path : getParts(dir, baseName + "\\.\\d+")) {
            String actualFileName = path.getFileName().toString();
            String expectedFileName = String.format("%s.%03d", baseName, i);

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

    private final long length;

    public SevenZipSplitSrcFile(Path path, List<Item> items, long length) {
        super(path, items);
        this.length = length;
    }

    public boolean isLast(Item item) {
        return item == null || items.size() < item.getPos();
    }

    @Override
    public DataInputFile dataInputFile() throws IOException {
        return new LittleEndianSevenZipReadFile(this);
    }

    @Override
    public boolean isSplit() {
        return false;
    }

}
