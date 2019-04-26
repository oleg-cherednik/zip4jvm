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

    public static final short SIGNATURE = 0x0001;

    // size:2 - tag for this "extra" block type (ZIP64 = 0x001)
    private final short signature = SIGNATURE;
    // size:2 - size of this "extra" block
    private int size;
    // size:8 - original uncompressed file size
    private long uncompressedSize = ExtraField.NO_DATA;
    // size:8 - size of compressed data
    private long compressedSize = ExtraField.NO_DATA;
    // size:8 - offset of local header record
    private long offsLocalHeaderRelative = ExtraField.NO_DATA;
    // size:4 - number of the disk on which  this file starts
    private int diskNumber = ExtraField.NO_DATA;

    public static final Zip64ExtendedInfo NULL = new Zip64ExtendedInfo() {

        @Override
        public void setSize(int size) {
            throw new NullPointerException("Null object modification: " + getClass().getSimpleName());
        }

        @Override
        public void setUncompressedSize(long uncompressedSize) {
            throw new NullPointerException("Null object modification: " + getClass().getSimpleName());
        }

        @Override
        public void setCompressedSize(long compressedSize) {
            throw new NullPointerException("Null object modification: " + getClass().getSimpleName());
        }

        @Override
        public void setOffsLocalHeaderRelative(long offsLocalHeaderRelative) {
            throw new NullPointerException("Null object modification: " + getClass().getSimpleName());
        }

        @Override
        public void setDiskNumber(int diskNumber) {
            throw new NullPointerException("Null object modification: " + getClass().getSimpleName());
        }
    };
}
