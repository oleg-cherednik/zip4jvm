package ru.olegcherednik.zip4jvm.io.enhanceddeflate;

/**
 * This interface provides statistics on the current decompression stream.
 * The stream consumer can use that statistics to handle abnormal
 * compression ratios, i.e. to prevent zip bombs.
 *
 * @since 1.17
 */
public interface InputStreamStatistics {

    /**
     * @return the amount of raw or compressed bytes read by the stream
     */
    long getCompressedCount();

    /**
     * @return the amount of decompressed bytes returned by the stream
     */
    long getUncompressedCount();
}
