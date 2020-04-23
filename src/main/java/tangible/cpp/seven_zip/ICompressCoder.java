package tangible.cpp.seven_zip;

import tangible.cpp.common.HRESULT;
import tangible.cpp.common.IUnknown;
import tangible.cpp.seven_zip.archive.ISequentialOutStream;

// ICoder.h24
public interface ICompressCoder extends IUnknown {

    HRESULT Code(ISequentialInStream inStream, ISequentialOutStream outStream, long inSize, long[] outSize,
            ICompressProgressInfo progress);
}
