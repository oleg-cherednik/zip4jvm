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

package net.lingala.zip4j.utils;

public interface InternalZipConstants {

    /*
     * Header signatures
     */
    // Whenever a new Signature is added here, make sure to add it
    // in Zip4jUtil.getAllHeaderSignatures()
    static long EXTSIG = 0x08074b50L;    // "PK\007\008"
    int DIGSIG = 0x05054b50;
    static long ARCEXTDATREC = 0x08064b50L;
    static int SPLITSIG = 0x08074b50;
    int ZIP64_ENDSIG = 0x06064b50;

    static final int STD_DEC_HDR_SIZE = 12;

    //AES Constants
    static final int AES_AUTH_LENGTH = 10;
    static final int AES_BLOCK_SIZE = 16;

    // Minimum segment size = 64K
    int MIN_SPLIT_LENGTH = 64 * 1024;

    /*
     * 4.3.9.2 When compressing files, compressed and uncompressed sizes
     * SHOULD be stored in ZIP64 format (as 8 byte values) when a
     * file's size exceeds 0xFFFFFFFF.   However ZIP64 format MAY be
     * used regardless of the size of a file.  When extracting, if
     * the zip64 extended information extra field is present for
     * the file the compressed and uncompressed sizes will be 8
     * byte values.
     */
    static final long ZIP_64_LIMIT = 0xFFFF_FFFFL;

    public static String OFFSET_CENTRAL_DIR = "offsetCentralDir";

    public static final String VERSION = "1.3.2";

    public static final int MODE_ZIP = 1;

    public static final int MODE_UNZIP = 2;

    public static final String WRITE_MODE = "rw";

    public static final String READ_MODE = "r";

    public static final int BUFF_SIZE = 1024 * 4;

    public static final int FILE_MODE_NONE = 0;

    public static final int FILE_MODE_READ_ONLY = 1;

    public static final int FILE_MODE_HIDDEN = 2;

    public static final int FILE_MODE_ARCHIVE = 32;

    public static final int FILE_MODE_READ_ONLY_HIDDEN = 3;

    public static final int FILE_MODE_READ_ONLY_ARCHIVE = 33;

    public static final int FILE_MODE_HIDDEN_ARCHIVE = 34;

    public static final int FILE_MODE_READ_ONLY_HIDDEN_ARCHIVE = 35;

    public static final int FILE_MODE_SYSTEM = 38;

    public static final int FOLDER_MODE_NONE = 16;

    public static final int FOLDER_MODE_HIDDEN = 18;

    public static final int FOLDER_MODE_ARCHIVE = 48;

    public static final int FOLDER_MODE_HIDDEN_ARCHIVE = 50;

    // Update local file header constants
    // This value holds the number of bytes to skip from
    // the offset of start of local header
    public static final int UPDATE_LFH_CRC = 14;

    public static final int UPDATE_LFH_COMP_SIZE = 18;

    public static final int UPDATE_LFH_UNCOMP_SIZE = 22;

    public static final int LIST_TYPE_FILE = 1;

    public static final int LIST_TYPE_STRING = 2;

    public static final int UFT8_NAMES_FLAG = 1 << 11;

    public static final int MAX_ALLOWED_ZIP_COMMENT_LENGTH = 0xFFFF;
}
