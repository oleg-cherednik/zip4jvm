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
public class EndOfCentralDirectory {

    // size with comment length = 0
    public static final int MIN_SIZE = 22;
    // max length for comment is 2 bytes
    public static final int MAX_COMMENT_LENGTH = 0xFFFF;

    // size:4 - signature (0x06054b50)
    private final int signature = InternalZipConstants.ENDSIG;
    // size:2 - number of the disk
    private int noOfThisDisk;
    // size:2 - number of the disk with the start of the central directory
    private int noOfThisDiskStartOfCentralDir;
    // size:2 - total number of entries in the central directory on this disk
    private int totNoOfEntriesInCentralDirOnThisDisk;
    // size:2 - total number of entries in the central directory
    private int totNoOfEntriesInCentralDir;
    // size:4 - size of the central directory
    private int sizeOfCentralDir;
    // size:4 - offset of start of central directory with respect to the starting disk number
    private long offsetOfStartOfCentralDir;
    // size:2 - file comment length (n)
    private int commentLength;
    // size:n - file comment
    private String comment;
    @Deprecated
    private byte[] commentBytes;

}
