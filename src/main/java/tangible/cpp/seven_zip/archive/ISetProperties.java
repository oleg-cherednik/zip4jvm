package tangible.cpp.seven_zip.archive;

import tangible.cpp.common.GUID;
import tangible.cpp.common.HRESULT;
import tangible.cpp.common.IUnknown;

import static tangible.cpp.common.GUID.groupId;
import static tangible.cpp.common.GUID.k_7zip_GUID_Data1;
import static tangible.cpp.common.GUID.k_7zip_GUID_Data2;
import static tangible.cpp.common.GUID.k_7zip_GUID_Data3_Common;

public interface ISetProperties extends IUnknown {

    byte subId = (byte)0x03;

    GUID IID_ISetProperties = new GUID(
            k_7zip_GUID_Data1,
            k_7zip_GUID_Data2,
            k_7zip_GUID_Data3_Common,
            new byte[] { 0x0, 0x0, 0x0, groupId, 0x0, subId, 0x0, 0x0 });

    HRESULT SetProperties(char[] names, PROPVARIANT values, long numProps);

}
