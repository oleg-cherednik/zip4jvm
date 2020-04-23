package tangible.cpp.seven_zip.archive;

import tangible.cpp.common.HRESULT;

public interface IArchiveExtractCallback {

    HRESULT GetStream(long index, ISequentialOutStream outStream, int askExtractMode);

    HRESULT PrepareOperation(int askExtractMode);

    HRESULT SetOperationResult(int resultEOperationResult);
}
