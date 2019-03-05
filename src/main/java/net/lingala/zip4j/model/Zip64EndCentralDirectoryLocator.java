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

@Getter
@Setter
public class Zip64EndCentralDirectoryLocator {

    // size (20) with comment length = 0
    public static final int SIZE = 4 + 4 + 8 + 4;

    // size:4 - signature (0x06054b50)
    private final int signature = InternalZipConstants.ZIP64_ENDSIG_LOC;
    // size:4 - number of the disk with the start of the zip64 end of central directory
    private int noOfDiskStartOfZip64EndOfCentralDirRec;
    // size:8 - relative offset of the zip64 end of central directory record
    private long offsetZip64EndOfCentralDirRec;
    // size:4 - total number of disks
    private int totNumberOfDiscs;

}
