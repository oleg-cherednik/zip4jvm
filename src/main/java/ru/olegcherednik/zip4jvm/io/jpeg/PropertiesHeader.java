package ru.olegcherednik.zip4jvm.io.jpeg;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 04.09.2020
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class PropertiesHeader {

    // size:1 - size of the properties header (in bytes) including this field
    private final int propertiesSize;
    // size:1
    private final Version versionInformation;
    // size:1 - JPEG compression method identifier (0 is explicitly invalid)
    private final int compressionMethod;
    // size:1
    private final Options options;

//        public int write(DataOutput out) throws IOException {
//            out.writeByte((byte)((pb * 5 + lp) * 9 + lc));
//            out.writeDword(dictionarySize);
//            return 5;
//        }

    public static PropertiesHeader read(DataInput in) throws IOException {
        int propertiesSize = in.readByte();
        Version versionInformation = Version.of(in.readByte());
        int compressionMethod = in.readByte();
        Options options = Options.of(in.readByte());
        return new PropertiesHeader(propertiesSize, versionInformation, compressionMethod, options);
    }

    @Getter
    @EqualsAndHashCode(doNotUseGetters = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Version {

        public static final Version NULL = new Version(0, 0);

        // size:[4-7] - major version
        private final int major;
        // size:[0-3] - minor version
        private final int minor;

        // only v1.0 is supported
        public static Version of(int data) {
            return new Version(data >> 4, data & 0xF);
        }

        @Override
        public String toString() {
            return this == NULL ? "<null>" : major + "." + minor;
        }

    }

    @Getter
    @EqualsAndHashCode(doNotUseGetters = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Options {

        // size:[0-4] - slice value (only for v1.0)
        private final int slice;

        // only v1.0 is supported
        public static Options of(int data) {
            return new Options(data & 0xF);
        }
    }
}
