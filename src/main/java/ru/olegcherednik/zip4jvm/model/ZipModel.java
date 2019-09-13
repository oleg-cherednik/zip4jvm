package ru.olegcherednik.zip4jvm.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.io.FilenameUtils;
import ru.olegcherednik.zip4jvm.exception.Zip4jException;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import java.nio.file.Files;
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
public class ZipModel {

    public static final int NO_SPLIT = -1;
    // MIN_SPLIT_LENGTH = 64K bytes
    public static final int MIN_SPLIT_SIZE = 64 * 1024;

    public static final int MAX_TOTAL_ENTRIES = Zip64.LIMIT_INT;
    public static final long MAX_ENTRY_SIZE = Zip64.LIMIT;
    public static final int MAX_TOTAL_DISKS = Zip64.LIMIT_INT;

    @NonNull
    private final Path file;
    private long splitSize = NO_SPLIT;
    private Path streamFile;

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

    public ZipModel(@NonNull Path file) {
        this.file = file;
        streamFile = file;
    }

    @Getter(AccessLevel.NONE)
    private final Map<String, ZipEntry> fileNameEntry = new LinkedHashMap<>();

    public void setSplitSize(long splitSize) {
        this.splitSize = Math.max(MIN_SPLIT_SIZE, splitSize);
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

    public void addEntry(@NonNull ZipEntry entry) {
        fileNameEntry.put(entry.getFileName(), entry);
    }

    public Collection<ZipEntry> getEntries() {
        return isEmpty() ? Collections.emptyList() : Collections.unmodifiableCollection(fileNameEntry.values());
    }

    public ZipEntry getEntryByFileName(@NonNull String fileName) {
        return fileNameEntry.get(fileName);
    }

    @NonNull
    public Set<String> getEntryNames() {
        return isEmpty() ? Collections.emptySet() : Collections.unmodifiableSet(fileNameEntry.keySet());
    }

    public Path getPartFile(long disk) {
        return disk == totalDisks ? file : getSplitFilePath(file, disk + 1);
    }

    public Path getStreamPartFile(long disk) {
        return disk == totalDisks ? streamFile : getSplitFilePath(streamFile, disk + 1);
    }

    @NonNull
    public ZipModel noSplitOnly() {
        if (Files.exists(file) && isSplit())
            throw new Zip4jException("Zip file already exists. Zip file format does not allow updating split/spanned files");

        return this;
    }

    public static Path getSplitFilePath(Path zip, long disk) {
        return zip.getParent().resolve(String.format("%s.z%02d", FilenameUtils.getBaseName(zip.toString()), disk));
    }

}
