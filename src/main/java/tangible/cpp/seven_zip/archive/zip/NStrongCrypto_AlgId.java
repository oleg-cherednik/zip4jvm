package tangible.cpp.seven_zip.archive.zip;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum NStrongCrypto_AlgId {
    kDES(0x6601),
    kRC2old(0x6602),
    k3DES168(0x6603),
    k3DES112(0x6609),
    kAES128(0x660E),
    kAES192(0x660F),
    kAES256(0x6610),
    kRC2(0x6702),
    kBlowfish(0x6720),
    kTwofish(0x6721),
    kRC4(0x6801);

    @Getter
    private final int id;
}
