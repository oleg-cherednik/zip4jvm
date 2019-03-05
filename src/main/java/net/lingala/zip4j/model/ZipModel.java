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

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.List;

@Getter
@Setter
public class ZipModel implements Cloneable {

    private List localFileHeaderList;

    private List dataDescriptorList;

    private ArchiveExtraDataRecord archiveExtraDataRecord;

    private CentralDirectory centralDirectory;

    private EndCentralDirectory endCentralDirectory;

    private Zip64EndCentralDirectoryLocator zip64EndCentralDirectoryLocator;

    private Zip64EndOfCentralDirectory zip64EndOfCentralDirectory;

    private boolean splitArchive;

    private long splitLength;

    private String zipFile;

    private boolean isZip64Format;

    private boolean isNestedZipFile;

    private long start;

    private long end;

    @NonNull
    private Charset charset = Charset.defaultCharset();

    public ZipModel() {
        splitLength = -1;
    }

    public List getLocalFileHeaderList() {
        return localFileHeaderList;
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

    public String getZipFile() {
        return zipFile;
    }

    public void setZipFile(String zipFile) {
        this.zipFile = zipFile;
    }

    public Zip64EndOfCentralDirectory getZip64EndOfCentralDirectory() {
        return zip64EndOfCentralDirectory;
    }

    public void setZip64EndOfCentralDirectory(
            Zip64EndOfCentralDirectory zip64EndOfCentralDirectory) {
        this.zip64EndOfCentralDirectory = zip64EndOfCentralDirectory;
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

    public long getSplitLength() {
        return splitLength;
    }

    public void setSplitLength(long splitLength) {
        this.splitLength = splitLength;
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

}
