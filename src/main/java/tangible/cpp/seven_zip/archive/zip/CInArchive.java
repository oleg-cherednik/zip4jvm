package tangible.cpp.seven_zip.archive.zip;

import tangible.cpp.common.HRESULT;
import tangible.cpp.seven_zip.ISequentialInStream;

import static tangible.cpp.common.HRESULT.S_OK;

// ZipIn.h:244
public class CInArchive {

    public boolean IsLocalOffsetOK(CItemEx item) {
//        if (item.FromLocal)
//            return true;
//        return (Int64)GetOffset() + (Int64)item.LocalHeaderPos >= 0;
        return false;
    }

    public long GetOffset() {
//        if (IsMultiVol)
//            return 0;
//        else
//            return ArcInfo.Base;
        return 0;
    }

    // ZipIn.h.cpp:1217
    public HRESULT ReadLocalItemAfterCdItem(CItemEx item, boolean[] isAvail, boolean[] headersError) {
        return S_OK;
    }

    // ZipIn.cpp:3195
    public HRESULT GetItemStream(CItemEx item, boolean seekPackData, ISequentialInStream[] stream) {
        return S_OK;
    }

}
