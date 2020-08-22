package tangible.cpp.seven_zip;

import tangible.cpp.common.GUID;
import tangible.cpp.common.HRESULT;

import static tangible.cpp.common.GUID.groupId;
import static tangible.cpp.common.GUID.k_7zip_GUID_Data1;
import static tangible.cpp.common.GUID.k_7zip_GUID_Data2;
import static tangible.cpp.common.GUID.k_7zip_GUID_Data3_Common;

// ICoder.h:340
public interface ICryptoSetPassword {

    byte subId = (byte)0x90;

    GUID IID_ICryptoSetPassword = new GUID(
            k_7zip_GUID_Data1,
            k_7zip_GUID_Data2,
            k_7zip_GUID_Data3_Common,
            new byte[] { 0x0, 0x0, 0x0, groupId, 0x0, subId, 0x0, 0x0 });


    HRESULT ResetInitVector();

    HRESULT CryptoSetPassword(byte[] data, int size);

}
