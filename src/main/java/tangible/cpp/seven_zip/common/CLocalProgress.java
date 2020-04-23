package tangible.cpp.seven_zip.common;

import tangible.cpp.common.CMyUnknownImp;
import tangible.cpp.common.GUID;
import tangible.cpp.common.HRESULT;
import tangible.cpp.seven_zip.ICompressProgressInfo;
import tangible.cpp.seven_zip.IProgress;

import static tangible.cpp.common.GUID.groupId;
import static tangible.cpp.common.GUID.k_7zip_GUID_Data1;
import static tangible.cpp.common.GUID.k_7zip_GUID_Data2;
import static tangible.cpp.common.GUID.k_7zip_GUID_Data3_Common;

// ProgressUtils.h:11
public class CLocalProgress extends CMyUnknownImp implements ICompressProgressInfo {

    byte subId = (byte)0x04;

    GUID IID_IInArchive = new GUID(
            k_7zip_GUID_Data1,
            k_7zip_GUID_Data2,
            k_7zip_GUID_Data3_Common,
            new byte[] { 0x0, 0x0, 0x0, groupId, 0x0, subId, 0x0, 0x0 });

    public long ProgressOffset;
    public long InSize;
    public long OutSize;
    public boolean SendRatio;
    public boolean SendProgress;

    public void Init(IProgress progress, boolean inSizeIsMain) {
//        _ratioProgress.Release();
//        _progress = progress;
//        _progress.QueryInterface(IID_ICompressProgressInfo, &_ratioProgress);
//        _inSizeIsMain = inSizeIsMain;
    }

    public HRESULT SetCur() {
        return HRESULT.S_OK;
    }

    @Override
    public HRESULT SetRatioInfo(int[] inSize, int[] outSize) {
        return null;
    }

    @Override
    public HRESULT QueryInterface(GUID iid, Object[] outObject) {
        return null;
    }
}
