package tangible.cpp.seven_zip.archive;

import tangible.cpp.common.GUID;
import tangible.cpp.common.HRESULT;
import tangible.cpp.common.IUnknown;

import static tangible.cpp.common.GUID.groupId;
import static tangible.cpp.common.GUID.k_7zip_GUID_Data1;
import static tangible.cpp.common.GUID.k_7zip_GUID_Data2;
import static tangible.cpp.common.GUID.k_7zip_GUID_Data3_Common;

public interface ICompressCodecsInfo extends IUnknown {

    byte subId = (byte)0x60;

    GUID IID_ICompressCodecsInfo = new GUID(
            k_7zip_GUID_Data1,
            k_7zip_GUID_Data2,
            k_7zip_GUID_Data3_Common,
            new byte[] { 0x0, 0x0, 0x0, groupId, 0x0, subId, 0x0, 0x0 });

    HRESULT GetNumMethods(long[] numMethods);

    HRESULT GetProperty(long[] index, long propID, PROPVARIANT value);

    HRESULT CreateDecoder(long index, GUID iid, Object coder);

    HRESULT CreateEncoder(long index, GUID iid, Object coder);
}
