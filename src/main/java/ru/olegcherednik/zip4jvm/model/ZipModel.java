package ru.olegcherednik.zip4jvm.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * zip64:
 * <ul>
 * <li>Number of Files Inside an Archive - 65,535 <b>(implemented)</b></li>
 * <li>Size of a File Inside an Archive [bytes] - 4,294,967,295 <b>(implemented)</b></li>
 * <li>Size of an Archive [bytes] - 4,294,967,295 <b>(not implemented)</b></li>
 * <li>Number of Segments in a Segmented Archive - 999 (spanning), 65,535 (splitting) <b>(implemented for splitting)</b></li>
 * <li>Central Directory Size [bytes] - 4,294,967,295 <b>(not implemented)</b></li>
 * </ul>
 * <p>
 * http://www.artpol-software.com/ZipArchive/KB/0610051629.aspx#limits
 *
 * @author Oleg Cherednik
 * @since 10.03.2019
 */
@Getter
@Setter
@RequiredArgsConstructor
public class ZipModel {

    public static final int NO_SPLIT = -1;
    public static final int MIN_SPLIT_SIZE = 64 * 1024; // 64Kb

    public static final int MAX_TOTAL_ENTRIES = Zip64.LIMIT_WORD;
    public static final long MAX_ENTRY_SIZE = Zip64.LIMIT_DWORD;
    public static final long MAX_CENTRAL_DIRECTORY_OFFS = Zip64.LIMIT_DWORD;
    public static final int MAX_TOTAL_DISKS = Zip64.LIMIT_WORD;
    public static final int MAX_COMMENT_LENGTH = Zip64.LIMIT_WORD;

    private final Path file;
    private long splitSize = NO_SPLIT;

    private String comment;
    private long totalDisks;
    private long mainDisk;
    private long centralDirectoryOffs;
    private long centralDirectorySize;

    /**
     * {@literal true} only if section {@link Zip64} exists. In other words, do set this to {@code true}, to write zip archive
     * in ZIP64 format.
     */
    private boolean zip64;

    @Getter(AccessLevel.NONE)
    private final Map<String, ZipEntry> fileNameEntry = new LinkedHashMap<>();

    public void setComment(String comment) {
        if (StringUtils.length(comment) > MAX_COMMENT_LENGTH)
            throw new IllegalArgumentException("File comment should be " + MAX_COMMENT_LENGTH + " characters maximum");
        this.comment = StringUtils.isEmpty(comment) ? null : comment;
    }

    public boolean isSplit() {
        return splitSize > 0 || totalDisks > 0;
    }

    public boolean isEmpty() {
        return fileNameEntry.isEmpty();
    }

    public int getTotalEntries() {
        return fileNameEntry.size();
    }

    public void addEntry(ZipEntry zipEntry) {
        fileNameEntry.put(zipEntry.getFileName(), zipEntry);
    }

    public Collection<ZipEntry> getEntries() {
        return isEmpty() ? Collections.emptyList() : Collections.unmodifiableCollection(fileNameEntry.values());
    }

    public ZipEntry getEntryByFileName(String fileName) {
        return fileNameEntry.get(fileName);
    }

    public boolean hasEntry(String fileName) {
        return fileNameEntry.containsKey(fileName);
    }

    public Set<String> getEntryNames() {
        return isEmpty() ? Collections.emptySet() : Collections.unmodifiableSet(fileNameEntry.keySet());
    }

    public Path getPartFile(long disk) {
        return disk >= totalDisks ? file : getSplitFilePath(file, disk + 1);
    }

    public static Path getSplitFilePath(Path zip, long disk) {
        return zip.getParent().resolve(String.format("%s.z%02d", FilenameUtils.getBaseName(zip.toString()), disk));
    }

}
