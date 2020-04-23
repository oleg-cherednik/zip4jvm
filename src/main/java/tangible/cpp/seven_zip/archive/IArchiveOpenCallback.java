package tangible.cpp.seven_zip.archive;

import tangible.cpp.common.GUID;
import tangible.cpp.common.HRESULT;

import static tangible.cpp.common.GUID.groupId;
import static tangible.cpp.common.GUID.k_7zip_GUID_Data1;
import static tangible.cpp.common.GUID.k_7zip_GUID_Data2;
import static tangible.cpp.common.GUID.k_7zip_GUID_Data3_Common;

/**
 * @author Oleg Cherednik
 * @since 23.04.2020
 */
public interface IArchiveOpenCallback {

    int subId = 0x10;

    GUID IID_IArchiveOpenCallback = new GUID(
            k_7zip_GUID_Data1,
            k_7zip_GUID_Data2,
            k_7zip_GUID_Data3_Common,
            new byte[] { 0x0, 0x0, 0x0, groupId, 0x0, subId, 0x0, 0x0 });

    HRESULT SetTotal(long[] files, long[] bytes);

    HRESULT SetCompleted(long[] files, long[] bytes);

}
