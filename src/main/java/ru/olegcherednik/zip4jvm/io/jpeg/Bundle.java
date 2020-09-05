package ru.olegcherednik.zip4jvm.io.jpeg;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 05.09.2020
 */
public class Bundle {

    public static Bundle read(DataInput in) throws IOException {
        Header primaryHeader = readPrimaryHeader(in);
        Header extensionHeader = primaryHeader == Header.NULL ? readExtensionHeader(in) : Header.NULL;

        return null;
    }

    private static Header readPrimaryHeader(DataInput in) throws IOException {
        int uncompressedSize = in.readWord();
        int compressedSize = in.readWord();
        return uncompressedSize == 0xFFFF && compressedSize == 0xFFFF ? Header.NULL : new Header(uncompressedSize, compressedSize);
    }

    private static Header readExtensionHeader(DataInput in) throws IOException {
        long uncompressedSize = in.readWord();
        long compressedSize = in.readWord();
        return uncompressedSize == 0xFFFFFFFF && compressedSize == 0xFFFFFFFF ? Header.NULL : new Header(uncompressedSize, compressedSize);
    }

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Header {

        public static final Header NULL = new Header(0, 0);

        private final long uncompressedSize;
        private final long compressedSize;

        @Override
        public String toString() {
            return this == NULL ? "<null>" : String.format("uncompressedSize: %d, compressedSize: %d", uncompressedSize, compressedSize);
        }

    }
}
