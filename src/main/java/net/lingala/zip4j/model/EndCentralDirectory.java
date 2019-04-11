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
import org.apache.commons.lang.StringUtils;

import java.nio.charset.Charset;

@Getter
@Setter
public class EndCentralDirectory {

    // size (22) with comment length = 0
    public static final int MIN_SIZE = 4 + 2 + 2 + 2 + 2 + 4 + 4 + 2;
    // max length for comment is 2 bytes
    public static final int MAX_COMMENT_LENGTH = 0xFFFF;

    // size:4 - signature (0x06054b50)
    private final int signature = InternalZipConstants.ENDSIG;
    // size:2 - number of the disk
    private int diskNumber;
    // size:2 - number of the disk with the start of the central directory
    private int startDiskNumber;
    // size:2 - total number of entries in the central directory on this disk
    private int diskEntries;
    // size:2 - total number of entries in the central directory
    private int totalEntries;
    // size:4 - size of the central directory
    private int size;
    // size:4 - offset of start of central directory with respect to the starting disk number
    private long offs;
    // size:2 - file comment length (n)
    private int commentLength;
    // size:n - file comment
    private String comment;
    private byte[] bufComment;

    public void setComment(String comment, Charset charset) {
        this.comment = comment;


        commentLength = StringUtils.length(comment);
    }

    public void incTotalEntries() {  g
        totalEntries++;
    }

    public void incDiskEntries() {
        diskEntries++;
    }
}
