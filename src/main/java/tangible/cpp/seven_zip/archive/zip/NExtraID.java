package tangible.cpp.seven_zip.archive.zip;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum NExtraID {
    kZip64(0x01),
    kNTFS(0x0A),
    kStrongEncrypt(0x17),
    kUnixTime(0x5455),
    kUnixExtra(0x5855),
    kIzUnicodeComment(0x6375),
    kIzUnicodeName(0x7075),
    kWzAES(0x9901);

    @Getter
    private final int id;
}
