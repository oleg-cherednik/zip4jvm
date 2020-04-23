package tangible.cpp.seven_zip;

import tangible.cpp.common.GUID;
import tangible.cpp.common.HRESULT;
import tangible.cpp.common.IUnknown;

import static tangible.cpp.common.GUID.groupId;
import static tangible.cpp.common.GUID.k_7zip_GUID_Data1;
import static tangible.cpp.common.GUID.k_7zip_GUID_Data2;
import static tangible.cpp.common.GUID.k_7zip_GUID_Data3_Common;

// ZipHandler.cpp:14
public interface ISequentialInStream extends IUnknown {

    byte subId = (byte)0x01;

    GUID IID_ISequentialInStream = new GUID(
            k_7zip_GUID_Data1,
            k_7zip_GUID_Data2,
            k_7zip_GUID_Data3_Common,
            new byte[] { 0x0, 0x0, 0x0, groupId, 0x0, subId, 0x0, 0x0 });

    HRESULT Read(byte[] data, long size, long processedSize);

}
