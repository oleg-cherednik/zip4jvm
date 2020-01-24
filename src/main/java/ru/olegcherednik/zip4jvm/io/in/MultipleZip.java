package ru.olegcherednik.zip4jvm.io.in;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Oleg Cherednik
 * @since 20.01.2020
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MultipleZip extends Zip {

    private final Path path;
    private final List<DiskInfo> items;
    private final long length;

    private MultipleZip(Path path, List<DiskInfo> items) {
        this.path = path;
        this.items = Collections.unmodifiableList(items);
        length = items.stream().mapToLong(DiskInfo::getSize).sum();
    }

    @Override
    public Path getDiskPath(int disk) {
        return items.size() < disk ? null : items.get(disk).getFile();
    }

    public DiskInfo getDisk(int disk) {
        return items.size() <= disk ? null : items.get(disk);
    }

    @Override
    public long getTotalDisks() {
        return items.size();
    }

    @Override
    public long length() {
        return length;
    }

    @Override
    public DataInputFile openDataInputFile() throws IOException {
        return new SevenLittleEndianReadFile(this);
    }

    public static MultipleZip create(Path zip) {
        Path parent = zip.getParent();
        String fileName = zip.getFileName().toString();

        if ("001".equals(FilenameUtils.getExtension(fileName)))
            fileName = fileName.substring(0, fileName.length() - 4);

        long offs = 0;
        List<DiskInfo> items = new LinkedList<>();

        for (int i = 0; ; i++) {
            Path file = parent.resolve(String.format("%s.%03d", fileName, i + 1));

            if (Files.exists(file)) {
                long size = getSize(file);
                items.add(DiskInfo.builder().disk(i).file(file).offs(offs).size(size).build());
                offs += size;
            } else
                break;
        }

        return new MultipleZip(parent.resolve(fileName), items);
    }

    private static long getSize(Path file) {
        try {
            return Files.size(file);
        } catch(IOException ignore) {
            return 0;
        }
    }

}
