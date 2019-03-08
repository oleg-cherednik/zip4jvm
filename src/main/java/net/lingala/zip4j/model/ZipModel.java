/*
 * Copyright 2010 Srikanth Reddy Lingala
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.lingala.zip4j.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FilenameUtils;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ZipModel implements Cloneable {

    public static final int NO_SPLIT = -1;

    private List localFileHeaderList;

    private List dataDescriptorList;

    private ArchiveExtraDataRecord archiveExtraDataRecord;

    private CentralDirectory centralDirectory;

    private EndCentralDirectory endCentralDirectory;

    private Zip64EndCentralDirectoryLocator zip64EndCentralDirectoryLocator;

    private Zip64EndCentralDirectory zip64EndCentralDirectory;

    private boolean splitArchive;
    private long splitLength = NO_SPLIT;

    private Path zipFile;

    private boolean isZip64Format;

    private boolean isNestedZipFile;

    private long start;

    private long end;

    @NonNull
    private Charset charset = Charset.defaultCharset();

    public List getLocalFileHeaderList() {
        return localFileHeaderList;
    }

    public void addLocalFileHeader(LocalFileHeader localFileHeader) {
        localFileHeaderList = localFileHeaderList.isEmpty() ? new ArrayList<>() : localFileHeaderList;
        localFileHeaderList.add(localFileHeader);
    }

    public void createEndCentralDirectoryIfNotExist() {
        if (endCentralDirectory == null)
            endCentralDirectory = new EndCentralDirectory();
    }

    public void setLocalFileHeaderList(List localFileHeaderList) {
        this.localFileHeaderList = localFileHeaderList;
    }

    public List getDataDescriptorList() {
        return dataDescriptorList;
    }

    public void setDataDescriptorList(List dataDescriptorList) {
        this.dataDescriptorList = dataDescriptorList;
    }

    public CentralDirectory getCentralDirectory() {
        return centralDirectory;
    }

    public void setCentralDirectory(CentralDirectory centralDirectory) {
        this.centralDirectory = centralDirectory;
    }

    public void setEndCentralDirectory(EndCentralDirectory endCentralDirectory) {
        this.endCentralDirectory = endCentralDirectory;
        splitArchive = endCentralDirectory != null && endCentralDirectory.getNoOfDisk() > 0;
    }

    public ArchiveExtraDataRecord getArchiveExtraDataRecord() {
        return archiveExtraDataRecord;
    }

    public void setArchiveExtraDataRecord(
            ArchiveExtraDataRecord archiveExtraDataRecord) {
        this.archiveExtraDataRecord = archiveExtraDataRecord;
    }

    public boolean isSplitArchive() {
        return splitArchive;
    }

    public void setSplitArchive(boolean splitArchive) {
        this.splitArchive = splitArchive;
    }

    public Zip64EndCentralDirectory getZip64EndCentralDirectory() {
        return zip64EndCentralDirectory;
    }

    public void setZip64EndCentralDirectory(
            Zip64EndCentralDirectory zip64EndCentralDirectory) {
        this.zip64EndCentralDirectory = zip64EndCentralDirectory;
    }

    public boolean isNestedZipFile() {
        return isNestedZipFile;
    }

    public void setNestedZipFile(boolean isNestedZipFile) {
        this.isNestedZipFile = isNestedZipFile;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public byte[] convertFileNameToByteArr(String fileName) throws UnsupportedEncodingException {
        return fileName.getBytes(charset);
    }

    public boolean isZip64Format() {
        return zip64EndCentralDirectoryLocator != null;
    }

    public boolean isEmpty() {
        return centralDirectory == null || centralDirectory.getFileHeaders().isEmpty();
    }

    public CentralDirectory.FileHeader getFileHeader(@NonNull String fileName) throws ZipException {
        if (isEmpty())
            return null;

        return centralDirectory.getFileHeaders().stream()
                               .filter(fileHeader -> FilenameUtils.equalsNormalized(fileName, fileHeader.getFileName()))
                               .findFirst().orElse(null);
    }

    public static Path getSplitFilePath(Path zipFile, int count) {
        return zipFile.getParent().resolve(String.format("%s.z%02d", FilenameUtils.getBaseName(zipFile.toString()), count));
    }

}
