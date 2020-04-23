package tangible.cpp.seven_zip.crypto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum EKeySizeMode {
    kKeySizeMode_AES128(1),
    kKeySizeMode_AES192(2),
    kKeySizeMode_AES256(3);

    @Getter
    private final int id;

}
