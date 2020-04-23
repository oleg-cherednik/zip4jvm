package tangible.cpp.seven_zip;

import tangible.cpp.common.GUID;
import tangible.cpp.common.HRESULT;

import static tangible.cpp.common.GUID.groupId;
import static tangible.cpp.common.GUID.k_7zip_GUID_Data1;
import static tangible.cpp.common.GUID.k_7zip_GUID_Data2;
import static tangible.cpp.common.GUID.k_7zip_GUID_Data3_Common;

// ICoder.h:181
public interface ICompressSetFinishMode {

    byte subId = (byte)0x26;

    GUID IID_ICompressSetFinishMode = new GUID(
            k_7zip_GUID_Data1,
            k_7zip_GUID_Data2,
            k_7zip_GUID_Data3_Common,
            new byte[] { 0x0, 0x0, 0x0, groupId, 0x0, subId, 0x0, 0x0 });

    /*
    0 : partial decoding is allowed. It's default mode for ICompressCoder::Code(), if (outSize) is defined.
    1 : full decoding. The stream must be finished at the end of decoding.
     */
    HRESULT SetFinishMode(long finishMode);

}
