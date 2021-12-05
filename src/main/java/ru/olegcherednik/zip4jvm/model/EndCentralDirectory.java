/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package ru.olegcherednik.zip4jvm.model;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ArrayUtils;

import java.nio.charset.Charset;

/**
 * see 4.3.16
 *
 * @author Oleg Cherednik
 * @since 26.04.2019
 */
@Getter
@Setter
public class EndCentralDirectory {

    public static final int SIGNATURE = 0x06054B50;

    // size (22) with comment length = 0
    public static final int MIN_SIZE = 4 + 2 + 2 + 2 + 2 + 4 + 4 + 2;

    // size:4 - signature (0x06054b50)
    // size:2 - number of the disk (=0 - single zip; >0 - split zip (e.g. 5 means 6 total parts))
    private int totalDisks;
    // size:2 - number of the disk with the central directory (single zip - 0; split zip - e.g. 5 means 6th part)
    private int mainDiskNo;
    // size:2 - total number of entries in the central directory on this disk
    private int diskEntries;
    // size:2 - total number of entries in the central directory
    private int totalEntries;
    // size:4 - CentralDirectory size
    private long centralDirectorySize;
    // size:4 - CentralDirectory offs
    private long centralDirectoryRelativeOffs;
    // size:2 - file comment length (n)
    // size:n - file comment
    private String comment;

    public byte[] getComment(Charset charset) {
        return comment == null ? ArrayUtils.EMPTY_BYTE_ARRAY : comment.getBytes(charset);
    }

}
