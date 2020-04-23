package tangible.cpp.seven_zip.archive.zip;

// ZipItem.h:116
public class CStrongCryptoExtra {

    int Format;
    int AlgId;
    int BitLen;
    int Flags;

    boolean ParseFromSubBlock(CExtraSubBlock sb) {
        if (sb.ID != NExtraID.kStrongEncrypt)
            return false;
        if (sb.Data.length < 8)
            return false;
//        Format = GetUi16(p + 0);
//        AlgId = GetUi16(p + 2);
//        BitLen = GetUi16(p + 4);
//        Flags = GetUi16(p + 6);
        return Format == 2;
    }

    boolean CertificateIsUsed() {
        return (Flags > 0x0001);
    }
}
