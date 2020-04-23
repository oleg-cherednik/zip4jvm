package tangible.cpp.seven_zip;

import tangible.cpp.common.GUID;
import tangible.cpp.common.HRESULT;

import static tangible.cpp.common.GUID.groupId;
import static tangible.cpp.common.GUID.k_7zip_GUID_Data1;
import static tangible.cpp.common.GUID.k_7zip_GUID_Data2;
import static tangible.cpp.common.GUID.k_7zip_GUID_Data3_Common;

// ICoder.h:171
public interface ICompressGetInStreamProcessedSize {

    byte subId = (byte)0x24;

    GUID IID_ICompressGetInStreamProcessedSize = new GUID(
            k_7zip_GUID_Data1,
            k_7zip_GUID_Data2,
            k_7zip_GUID_Data3_Common,
            new byte[] { 0x0, 0x0, 0x0, groupId, 0x0, subId, 0x0, 0x0 });

    HRESULT GetInStreamProcessedSize(long[] value);
}
