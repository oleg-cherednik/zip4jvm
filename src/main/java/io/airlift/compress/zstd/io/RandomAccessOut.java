package io.airlift.compress.zstd.io;

import io.airlift.compress.zstd.ByteArrayWithOffs;
import io.airlift.compress.zstd.dep.InByteArrayWithOffs;
import lombok.Getter;
import lombok.Setter;

import static io.airlift.compress.zstd.Constants.SIZE_OF_BYTE;
import static io.airlift.compress.zstd.Constants.SIZE_OF_INT;
import static io.airlift.compress.zstd.Constants.SIZE_OF_LONG;
import static io.airlift.compress.zstd.Constants.SIZE_OF_SHORT;

/**
 * @author Oleg Cherednik
 * @since 17.11.2026
 */
public interface RandomAccessOut extends Out {

}
