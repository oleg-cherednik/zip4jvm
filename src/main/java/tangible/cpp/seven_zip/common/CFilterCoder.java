package tangible.cpp.seven_zip.common;

import tangible.cpp.common.GUID;
import tangible.cpp.common.HRESULT;
import tangible.cpp.seven_zip.ICompressFilter;
import tangible.cpp.seven_zip.ICompressSetInStream;
import tangible.cpp.seven_zip.ISequentialInStream;

import static tangible.cpp.common.HRESULT.S_OK;

// FilterCoder.h:20
public class CFilterCoder implements ISequentialInStream, ICompressSetInStream {

    public ICompressFilter Filter;

    // FilterCoder.cpp:70
    public CFilterCoder(boolean encodeMode) {

    }

    @Override
    public HRESULT Read(byte[] data, long size, long processedSize) {
        return null;
    }

    @Override
    public HRESULT QueryInterface(GUID iid, Object[] outObject) {
        return null;
    }

    @Override
    public long AddRef() {
        return 0;
    }

    @Override
    public long Release() {
        return 0;
    }

    @Override
    public HRESULT SetInStream(ISequentialInStream inStream) {
        return null;
    }

    @Override
    public HRESULT ReleaseInStream() {
        return null;
    }

    // FilterCoder.h:110
    public class C_InStream_Releaser {
        public CFilterCoder FilterCoder;

    }

    // FilterCoder.h:126
    public class C_Filter_Releaser {

        public CFilterCoder FilterCoder;

    }

    // FilterCoder.h:202
    public HRESULT Init_NoSubFilterInit() {
        return S_OK;
    }

}
