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

@Getter
@Setter
public class Zip64ExtendedInfo {

    // size:2 - tag for this "extra" block type (ZIP64 = 0x001)
    private final int header = ExtraDataRecord.HEADER_ZIP64;
    // size:2 - size of this "extra" block
    private int size;
    // size:8 - original uncompressed file size
    private long unCompressedSize = -1;
    // size:8 - size of compressed data
    private long compressedSize = -1;
    // size:8 - offset of local header record
    private long offsLocalHeaderRelative = -1;
    // size:4 - number of the disk on which  this file starts
    private int diskNumberStart = -1;

}
