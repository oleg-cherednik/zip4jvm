package tangible.cpp.seven_zip.archive.zip;

import tangible.cpp.seven_zip.crypto.EKeySizeMode;

// ZipItem.h:59
public class CWzAesExtra {

    public static final int k_WzAesExtra_Size = 7;
    public int VendorVersion = 2; // 1: AE-1, 2: AE-2,
    public EKeySizeMode Strength; // 1: 128-bit, 2: 192-bit, 3: 256-bit
    public NFileHeader.NCompressionMethod.EType Method;

    // ZipItem.hL70
    public boolean ParseFromSubBlock(CExtraSubBlock sb) {
        if (sb.ID != NExtraID.kWzAES)
            return false;
        if (sb.Data.length < k_WzAesExtra_Size)
            return false;
//        const Byte * p = (const Byte *)sb.Data;
//        VendorVersion = GetUi16(p);
//        if (p[2] != 'A' || p[3] != 'E')
//            return false;
//        Strength = p[4];
//        // 9.31: The BUG was fixed:
//        Method = GetUi16(p + 5);
        return true;
    }

    public boolean NeedCrc() {
        return VendorVersion == 1;
    }
}
