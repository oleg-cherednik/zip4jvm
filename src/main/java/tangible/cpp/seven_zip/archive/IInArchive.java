package tangible.cpp.seven_zip.archive;

import tangible.cpp.common.GUID;
import tangible.cpp.common.HRESULT;

import java.io.InputStream;

import static tangible.cpp.common.GUID.groupId;
import static tangible.cpp.common.GUID.k_7zip_GUID_Data1;
import static tangible.cpp.common.GUID.k_7zip_GUID_Data2;
import static tangible.cpp.common.GUID.k_7zip_GUID_Data3_Common;

public interface IInArchive extends IArchive {

    byte subId = (byte)0x60;

    GUID IID_IInArchive = new GUID(
            k_7zip_GUID_Data1,
            k_7zip_GUID_Data2,
            k_7zip_GUID_Data3_Common,
            new byte[] { 0x0, 0x0, 0x0, groupId, 0x0, subId, 0x0, 0x0 });

    HRESULT Open(InputStream stream, long[] maxCheckStartPosition, IArchiveOpenCallback openCallback);

    HRESULT Close();

    HRESULT GetNumberOfItems(long[] numItems);

    HRESULT GetProperty(long index, long propID, PROPVARIANT value);

    HRESULT Extract(int[] indices, long numItems, boolean testMode, IArchiveExtractCallback extractCallback);

    HRESULT GetArchiveProperty(long propID, PROPVARIANT value);

    HRESULT GetNumberOfProperties(long[] numProps);

    HRESULT GetPropertyInfo(long index, char[] name, long[] propID, int[] varType);

    HRESULT GetNumberOfArchiveProperties(long[] numProps);

    HRESULT GetArchivePropertyInfo(long index, char[] name, long[] propID, int[] varType);
}
