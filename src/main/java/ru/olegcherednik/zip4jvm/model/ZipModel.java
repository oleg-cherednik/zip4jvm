package ru.olegcherednik.zip4jvm.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import ru.olegcherednik.zip4jvm.exception.EntryNotFoundException;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireMaxSizeComment;

/**
 * @author Oleg Cherednik
 * @since 10.03.2019
 */
@Getter
@Setter
@RequiredArgsConstructor
public final class ZipModel {

    public static final int NO_SPLIT = -1;
    public static final int MIN_SPLIT_SIZE = 64 * 1024; // 64Kb

    public static final int MAX_TOTAL_ENTRIES = Zip64.LIMIT_WORD;
    public static final long MAX_ENTRY_SIZE = Zip64.LIMIT_DWORD;
    public static final long MAX_CENTRAL_DIRECTORY_OFFS = Zip64.LIMIT_DWORD;
    public static final long MAX_LOCAL_FILE_HEADER_OFFS = Zip64.LIMIT_DWORD;
    public static final int MAX_TOTAL_DISKS = Zip64.LIMIT_WORD;
    public static final int MAX_COMMENT_SIZE = Zip64.LIMIT_WORD;

    private final SrcZip srcZip;
    private long splitSize = NO_SPLIT;

    private String comment;
    private int totalDisks;
    private long mainDiskNo;
    private long centralDirectoryRelativeOffs;
    private long centralDirectorySize;

    /**
     * {@literal true} only if section {@link Zip64} exists. In other words, do set this to {@code true}, to write zip archive
     * in ZIP64 format.
     */
    private boolean zip64;

    @Getter(AccessLevel.NONE)
    private final Map<String, ZipEntry> fileNameEntry = new LinkedHashMap<>();

    public void setComment(String comment) {
        requireMaxSizeComment(comment, MAX_COMMENT_SIZE);
        this.comment = StringUtils.isEmpty(comment) ? null : comment;
    }

    public boolean isSplit() {
        return splitSize != NO_SPLIT || totalDisks > 0;
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

    public Collection<ZipEntry> getZipEntries() {
        return isEmpty() ? Collections.emptyList() : Collections.unmodifiableCollection(fileNameEntry.values());
    }

    public ZipEntry getZipEntryByFileName(String fileName) {
        if (fileNameEntry.containsKey(fileName))
            return fileNameEntry.get(fileName);
        throw new EntryNotFoundException(fileName);
    }

    public boolean hasEntry(String fileName) {
        return fileNameEntry.containsKey(fileName);
    }

    public Set<String> getEntryNames() {
        return isEmpty() ? Collections.emptySet() : Collections.unmodifiableSet(fileNameEntry.keySet());
    }

    // TODO it seems that it should be moved to SrcZip
    public Path getDiskFile(int diskNo) {
        return diskNo >= totalDisks ? srcZip.getPath() : SrcZip.getDiskFile(srcZip.getPath(), diskNo + 1);
    }

}
