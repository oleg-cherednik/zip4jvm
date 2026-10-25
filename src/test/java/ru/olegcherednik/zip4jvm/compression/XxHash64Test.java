/*
 * Copyright 2019 Oleg Cherednik (oleg.cherednik@gmail.com)
 *
 * Licensed under The Apache Software License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
package ru.olegcherednik.zip4jvm.compression;

import com.github.luben.zstd.ZstdCompressCtx;
import io.airlift.compress.zstd.compress.XxHash64;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Oleg Cherednik
 * @since 25.10.2026
 */
@Test
public class XxHash64Test {

    // reference values of XXH64 with seed 0
    public void shouldReturnReferenceValueWhenKnownInput() {
        assertThat(hash(new byte[0])).isEqualTo(0xEF46DB3751D8E999L);
        assertThat(hash("abc".getBytes(StandardCharsets.US_ASCII))).isEqualTo(0x44BC2CF5AD770999L);
    }

    // Content_Checksum written by the reference zstd is the lower 32 bits of XXH64; lengths cover the short input,
    // the body and every combination of the tail steps
    public void shouldMatchZstdContentChecksumWhenAnyLength() {
        Random random = new Random(42);

        for (int length = 0; length <= 300; length++) {
            byte[] data = new byte[length];
            random.nextBytes(data);

            assertThat((int) hash(data)).as("length %d", length).isEqualTo(zstdContentChecksum(data));
        }
    }

    public void shouldReturnSameValueWhenDataIsFedByChunks() {
        Random random = new Random(42);
        byte[] data = new byte[10_015];
        random.nextBytes(data);

        long expected = hash(data);

        for (int i = 0; i < 100; i++) {
            XxHash64 checksum = new XxHash64();

            for (int off = 0; off < data.length; ) {
                int len = Math.min(random.nextInt(70), data.length - off);
                checksum.update(data, off, len);
                off += len;
            }

            assertThat(checksum.getValue()).isEqualTo(expected);
        }
    }

    public void shouldReturnSameValueWhenDataIsFedByBytes() {
        byte[] data = new byte[1000];
        new Random(42).nextBytes(data);

        XxHash64 checksum = new XxHash64();

        for (byte b : data)
            checksum.update(b);

        assertThat(checksum.getValue()).isEqualTo(hash(data));
    }

    public void shouldContinueHashingWhenValueIsRetrieved() {
        byte[] data = new byte[100];
        new Random(42).nextBytes(data);

        XxHash64 checksum = new XxHash64();
        checksum.update(data, 0, 40);
        checksum.getValue();
        checksum.update(data, 40, 60);

        assertThat(checksum.getValue()).isEqualTo(hash(data));
    }

    public void shouldStartFromScratchWhenReset() {
        byte[] data = new byte[100];
        new Random(42).nextBytes(data);

        XxHash64 checksum = new XxHash64();
        checksum.update(data, 0, 77);
        checksum.reset();
        checksum.update(data);

        assertThat(checksum.getValue()).isEqualTo(hash(data));
    }

    private static long hash(byte[] data) {
        XxHash64 checksum = new XxHash64();
        checksum.update(data);
        return checksum.getValue();
    }

    private static int zstdContentChecksum(byte[] data) {
        try (ZstdCompressCtx ctx = new ZstdCompressCtx()) {
            ctx.setChecksum(true);
            byte[] frame = ctx.compress(data);
            int offs = frame.length - 4;

            return (frame[offs] & 0xFF) | (frame[offs + 1] & 0xFF) << 8
                    | (frame[offs + 2] & 0xFF) << 16 | (frame[offs + 3] & 0xFF) << 24;
        }
    }

}
