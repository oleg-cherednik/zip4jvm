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
import lombok.Setter;
import net.lingala.zip4j.util.InternalZipConstants;
import org.apache.commons.lang.ArrayUtils;

/**
 * see 4.3.14  Zip64 end of central directory record
 *
 * @author Oleg Cherednik
 * @since 04.03.2019
 */
@Getter
@Setter
public class Zip64EndCentralDirectory {

    // size (44) with extensibleDataSector length = 0
    public static final int SIZE = 2 + 2 + 4 + 4 + 8 + 8 + 8 + 8;

    // size:4 - signature (0x06064b50)
    private final int signature = InternalZipConstants.ZIP64_ENDSIG;
    // size:8 - directory record (n)
    private long sizeOfZip64EndCentralDirRec;
    // size:2 - version made by
    private short versionMadeBy;
    // size:2 - version needed to extractEntries
    private short versionNeededToExtract;
    // size:4 - number of this disk
    private int noOfThisDisk;
    // size:4 - number of the disk with the start of the central directory
    private int noOfThisDiskStartOfCentralDir;
    // size:8 - total number of entries in the central directory on this disk
    private long totNoOfEntriesInCentralDirOnThisDisk;
    // size:8 - total number of entries in the central directory
    private long totalEntries;
    // size:8 - size of the central directory
    private long sizeOfCentralDir;
    // size:8 - directory with respect to the starting disk number
    private long offs;
    // size:n-44 - extensible data sector
    private byte[] extensibleDataSector;

    public void setExtensibleDataSector(byte[] extensibleDataSector) {
        this.extensibleDataSector = extensibleDataSector;
        sizeOfZip64EndCentralDirRec = SIZE + ArrayUtils.getLength(extensibleDataSector);
    }

    public void updateOffsetStartCenDirWRTStartDiskNo(long delta) {
        offs += delta;
    }

}
