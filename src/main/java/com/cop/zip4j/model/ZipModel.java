package com.cop.zip4j.model;

import com.cop.zip4j.exception.Zip4jException;
import com.cop.zip4j.model.entry.PathZipEntry;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.io.FilenameUtils;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    // MIN_SPLIT_LENGTH = 64K bytes
    public static final int MIN_SPLIT_LENGTH = 64 * 1024;

    public static final int MAX_TOTAL_ENTRIES = Zip64.LIMIT_INT;
    public static final long MAX_ENTRY_SIZE = Zip64.LIMIT;
    public static final int MAX_TOTAL_DISKS = Zip64.LIMIT_INT;

    @NonNull
    private final Path zipFile;
    @NonNull
    private final Charset charset;
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

    private final List<PathZipEntry> entries = new ArrayList<>();

    public void setSplitSize(long splitSize) {
        this.splitSize = splitSize < MIN_SPLIT_LENGTH ? NO_SPLIT : splitSize;
    }

    public boolean isSplit() {
        return splitSize > 0 || totalDisks > 0;
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public List<String> getEntryNames() {
        return entries.stream()
                      .map(PathZipEntry::getFileName)
                      .collect(Collectors.toList());
    }

    public static Path getSplitFilePath(Path zipFile, long disk) {
        return zipFile.getParent().resolve(String.format("%s.z%02d", FilenameUtils.getBaseName(zipFile.toString()), disk));
    }

    public Path getPartFile(long disk) {
        return disk == totalDisks ? zipFile : getSplitFilePath(zipFile, disk + 1);
    }

    @NonNull
    public ZipModel noSplitOnly() {
        if (Files.exists(zipFile) && isSplit())
            throw new Zip4jException("Zip file already exists. Zip file format does not allow updating split/spanned files");

        return this;
    }

}
