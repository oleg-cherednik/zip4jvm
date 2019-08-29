package com.cop.zip4j.model;

import com.cop.zip4j.exception.Zip4jException;
import com.cop.zip4j.model.activity.Activity;
import com.cop.zip4j.model.activity.PlainActivity;
import com.cop.zip4j.model.activity.Zip64Activity;
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
 * 1. Size of archive is over 4Gb (2^32)
 * 2. Total entries is over 65535 (2^16 - 1)
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

    @NonNull
    private final Path zipFile;
    @NonNull
    private final Charset charset;
    private long splitLength = NO_SPLIT;
    private Activity activity = PlainActivity.INSTANCE;

    private String comment;
    private int splitParts;
    private long centralDirectoryOffs;
    private long centralDirectorySize;
    private int startDiskNumber;

    private final List<PathZipEntry> entries = new ArrayList<>();

    public void setSplitLength(long splitLength) {
        this.splitLength = splitLength < MIN_SPLIT_LENGTH ? NO_SPLIT : splitLength;
    }

    public boolean isSplitArchive() {
        return splitLength > 0 || splitParts > 0;
    }

    public void zip64() {
        if (!(activity instanceof Zip64Activity))
            activity = Zip64Activity.INSTANCE;
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public List<String> getEntryNames() {
        return entries.stream()
                      .map(PathZipEntry::getName)
                      .collect(Collectors.toList());
    }

    public static Path getSplitFilePath(Path zipFile, int count) {
        return zipFile.getParent().resolve(String.format("%s.z%02d", FilenameUtils.getBaseName(zipFile.toString()), count));
    }

    public Path getPartFile(int diskNumber) {
        return diskNumber == splitParts ? zipFile : getSplitFilePath(zipFile, diskNumber + 1);
    }

    @NonNull
    public ZipModel noSplitOnly() {
        if (Files.exists(zipFile) && isSplitArchive())
            throw new Zip4jException("Zip file already exists. Zip file format does not allow updating split/spanned files");

        return this;
    }

}
