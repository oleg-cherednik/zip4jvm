package tangible.cpp.seven_zip;

import tangible.cpp.common.GUID;
import tangible.cpp.common.HRESULT;

import static tangible.cpp.common.GUID.groupId;
import static tangible.cpp.common.GUID.k_7zip_GUID_Data1;
import static tangible.cpp.common.GUID.k_7zip_GUID_Data2;
import static tangible.cpp.common.GUID.k_7zip_GUID_Data3_Common;

// ICoder.h:155
public interface ICompressSetDecoderProperties2 {

    byte subId = (byte)0x22;

    GUID IID_ICompressSetDecoderProperties2 = new GUID(
            k_7zip_GUID_Data1,
            k_7zip_GUID_Data2,
            k_7zip_GUID_Data3_Common,
            new byte[] { 0x0, 0x0, 0x0, groupId, 0x0, subId, 0x0, 0x0 });

    /* returns:
      S_OK
      E_NOTIMP      : unsupported properties
      E_INVALIDARG  : incorrect (or unsupported) properties
      E_OUTOFMEMORY : memory allocation error
    */
    HRESULT SetDecoderProperties2(int data, long size);
}
