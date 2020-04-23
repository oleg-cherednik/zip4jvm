package tangible.cpp.common;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class GUID {
    public static final long k_7zip_GUID_Data1 = 0x23170F69;
    public static final int k_7zip_GUID_Data2 = 0x40C1;
    public static final int k_7zip_GUID_Data3_Common = 0x278A;

    public static final int groupId = 0x6;

    private final long Data1;
    private final int Data2;
    private final int Data3;
    private final byte[] Data4; // new byte[8];


}
