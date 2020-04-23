package tangible.cpp.seven_zip.archive.zip;

import tangible.cpp.common.HRESULT;

// ZipIn.h:244
public class CInArchive {

    public boolean IsLocalOffsetOK(CItemEx item) {
        if (item.FromLocal)
            return true;
        return (Int64)GetOffset() + (Int64)item.LocalHeaderPos >= 0;
    }

    public long GetOffset() {
        if (IsMultiVol)
            return 0;
        else
            return ArcInfo.Base;
    }

    // ZipIn.h.cpp:1217
    public HRESULT ReadLocalItemAfterCdItem(CItemEx item, boolean[] isAvail, boolean[] headersError) {
        return HRESULT.S_OK;
    }

}
