package tangible.cpp.seven_zip.archive;

import tangible.cpp.common.GUID;
import tangible.cpp.common.HRESULT;
import tangible.cpp.seven_zip.IProgress;
import tangible.cpp.seven_zip.archive.zip.NAskMode;
import tangible.cpp.seven_zip.archive.zip.NExtract;

import static tangible.cpp.common.GUID.groupId;
import static tangible.cpp.common.GUID.k_7zip_GUID_Data1;
import static tangible.cpp.common.GUID.k_7zip_GUID_Data2;
import static tangible.cpp.common.GUID.k_7zip_GUID_Data3_Common;

// IArchive.h:179
public interface IArchiveExtractCallback extends IProgress {

    byte subId = (byte)0x20;

    GUID IID_IArchiveExtractCallback = new GUID(
            k_7zip_GUID_Data1,
            k_7zip_GUID_Data2,
            k_7zip_GUID_Data3_Common,
            new byte[] { 0x0, 0x0, 0x0, groupId, 0x0, subId, 0x0, 0x0 });

    HRESULT GetStream(long index, ISequentialOutStream[] outStream, NAskMode askExtractMode);

    HRESULT PrepareOperation(NAskMode askExtractMode);

    HRESULT SetOperationResult(NExtract.NOperationResult resultEOperationResult);

    HRESULT SetTotal(long total);

    HRESULT SetCompleted(long[] completeValue);
}
