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
package ru.olegcherednik.zip4jvm.io.lzma;

import lombok.Getter;
import org.apache.commons.io.IOUtils;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.in.data.DataInputFile;
import ru.olegcherednik.zip4jvm.io.lzma.lz.MatchFinder;
import ru.olegcherednik.zip4jvm.io.out.data.DataOutput;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Oleg Cherednik
 * @since 14.02.2020
 */
public class LzmaInputStream extends InputStream {

    private final LzmaDecoder lzma;
    private final byte[] buf = new byte[1];

    private boolean endReached;
    /* Number of uncompressed bytes left to be decompressed, or -1 if the end marker is used. */
    private long remainingSize;

    public LzmaInputStream(DataInput in, long uncompressedSize) throws IOException {
        lzma = LzmaDecoder.create(in);
        remainingSize = uncompressedSize;
    }

    @Override
    public int read() throws IOException {
        return read(buf, 0, 1) == -1 ? -1 : (buf[0] & 0xFF);
    }

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        if (len == 0)
            return 0;
        if (endReached)
            return IOUtils.EOF;

        int size = 0;

        while (len > 0) {
            // If uncompressed size is known and thus no end marker will be present, set the limit so that the uncompressed size won't be exceeded.
            int copySizeMax = len;
            if (remainingSize >= 0 && remainingSize < len)
                copySizeMax = (int)remainingSize;

            lzma.getLz().setLimit(copySizeMax);

            // Decode into the dictionary buffer.
            try {
                lzma.decode();
            } catch(LzmaCorruptedInputException e) {
                /*
                 * The end marker is encoded with a LZMA symbol that indicates maximum match distance. This is larger than any supported dictionary
                 * and thus causes CorruptedInputException from LZDecoder.repeat.
                 */
                if (remainingSize != -1 || !lzma.isEndMarkerDetected())
                    throw e;

                endReached = true;

                /*
                 * The exception makes lzma.decode() miss the last range decoder normalization, so do it here. This might cause an IOException if it
                 * needs to read a byte from the input stream.
                 */
                lzma.getRaceDecoder().normalize();
            }

            // Copy from the dictionary to buf.
            int copiedSize = lzma.getLz().flush(buf, offs);
            offs += copiedSize;
            len -= copiedSize;
            size += copiedSize;

            if (remainingSize >= 0) {
                // Update the number of bytes left to be decompressed.
                remainingSize -= copiedSize;

                if (remainingSize == 0)
                    endReached = true;
            }

            if (endReached) {
// TODO it works with 7-ZIP but does not work with SecureZIP
//                if (!lzma.getRaceDecoder().isFinished() || lzma.getLz().hasPending())
//                    throw new LzmaCorruptedInputException();

                return size == 0 ? -1 : size;
            }
        }

        return size;
    }

    @Override
    public void close() throws IOException {
        lzma.close();
    }

    @Getter
    public static class Properties {

        private static final int LC_DEFAULT = 3;
        private static final int LP_DEFAULT = 0;
        private static final int PB_DEFAULT = 2;
        private static final int NICE_LENGTH_MAX = 273;

        private static final int[] PRESET_TO_DICTIONARY_SIZE = {
                1 << 18, 1 << 20, 1 << 21, 1 << 22, 1 << 22,
                1 << 23, 1 << 23, 1 << 24, 1 << 25, 1 << 26 };

        private static final int[] PRESET_TO_DEPTH_LIMIT = { 4, 8, 24, 48 };

        private final int dictionarySize;
        private final int lc;
        private final int lp;
        private final int pb;
        private final int niceLength;
        private final int depthLimit;
        private final Mode mode;
        private final MatchFinder matchFinder;

        public Properties(CompressionLevel compressionLevel) {
            lc = LC_DEFAULT;
            lp = LP_DEFAULT;
            pb = PB_DEFAULT;
            dictionarySize = PRESET_TO_DICTIONARY_SIZE[compressionLevel.getCode()];

            if (compressionLevel == CompressionLevel.SUPER_FAST) {
                mode = Mode.FAST;
                matchFinder = MatchFinder.HASH_CHAIN;
                niceLength = 128;
                depthLimit = PRESET_TO_DEPTH_LIMIT[CompressionLevel.SUPER_FAST.getCode()];
            } else if (compressionLevel == CompressionLevel.FAST) {
                mode = Mode.FAST;
                matchFinder = MatchFinder.HASH_CHAIN;
                niceLength = NICE_LENGTH_MAX;
                depthLimit = PRESET_TO_DEPTH_LIMIT[CompressionLevel.FAST.getCode()];
            } else if (compressionLevel == CompressionLevel.NORMAL) {
                mode = Mode.NORMAL;
                matchFinder = MatchFinder.BINARY_TREE;
                niceLength = 32;
                depthLimit = 0;
            } else {
                mode = Mode.NORMAL;
                matchFinder = MatchFinder.BINARY_TREE;
                niceLength = 64;
                depthLimit = 0;
            }
        }

        public int write(DataOutput out) throws IOException {
            out.writeByte((byte)((pb * 5 + lp) * 9 + lc));
            out.writeDword(dictionarySize);
            return 5;
        }

    }
}
