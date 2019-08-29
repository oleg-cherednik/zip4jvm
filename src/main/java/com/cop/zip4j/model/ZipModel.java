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
    @NonNull
    private EndCentralDirectory endCentralDirectory = new EndCentralDirectory();
    @NonNull
    private Zip64 zip64 = Zip64.NULL;
    private long splitLength = NO_SPLIT;
    private Activity activity = new PlainActivity();

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

    public void setZip64(Zip64 zip64) {
        this.zip64 = zip64;
        activity = zip64 == Zip64.NULL ? new PlainActivity() : new Zip64Activity();
    }

    public void zip64() {
        if (zip64 == Zip64.NULL)
            setZip64(Zip64.of(new Zip64.EndCentralDirectoryLocator(), new Zip64.EndCentralDirectory()));
    }

    public void updateZip64(int counter) {
        if (zip64 == Zip64.NULL)
            return;

        Zip64.EndCentralDirectory dir = zip64.getEndCentralDirectory();
        dir.setSize(Zip64.EndCentralDirectory.SIZE + dir.getSizeEndCentralDirectory());
        dir.setVersionMadeBy(CentralDirectory.FileHeader.VERSION);
        dir.setVersionNeededToExtract(CentralDirectory.FileHeader.VERSION);
        dir.setDisk(splitLength);
        dir.setStartDisk(startDiskNumber);
        dir.setDiskEntries(countNumberOfFileHeaderEntriesOnDisk());
        dir.setTotalEntries(entries.size());
        dir.setSize(centralDirectorySize);
        dir.setCentralDirectoryOffs(centralDirectoryOffs);

        Zip64.EndCentralDirectoryLocator locator = zip64.getEndCentralDirectoryLocator();
        locator.setOffs(centralDirectoryOffs + centralDirectorySize);
        locator.setStartDisk(counter);
        locator.setTotalDisks(counter + 1);
    }

    private int countNumberOfFileHeaderEntriesOnDisk() {
        if (isSplitArchive())
            return (int)entries.stream()
                               .filter(entry -> entry.getDisc() == splitParts)
                               .count();

        return entries.size();

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
