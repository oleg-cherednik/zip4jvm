package tangible.cpp.seven_zip.archive;

import tangible.cpp.common.HRESULT;
import tangible.cpp.seven_zip.IProgress;
import tangible.cpp.seven_zip.archive.zip.NAskMode;
import tangible.cpp.seven_zip.archive.zip.NOperationResult;

public interface IArchiveExtractCallback extends IProgress {

    HRESULT GetStream(long index, ISequentialOutStream[] outStream, NAskMode askExtractMode);

    HRESULT PrepareOperation(NAskMode askExtractMode);

    HRESULT SetOperationResult(NOperationResult resultEOperationResult);

    HRESULT SetTotal(long total);

    HRESULT SetCompleted(long[] completeValue);
}
