package tangible.cpp.seven_zip.archive.common;

import tangible.cpp.common.CMyUnknownImp;
import tangible.cpp.common.GUID;
import tangible.cpp.common.HRESULT;
import tangible.cpp.seven_zip.ISequentialInStream;

import static tangible.cpp.common.HRESULT.S_OK;

// LimitedStreams.h:11
public class CLimitedSequentialInStream extends CMyUnknownImp implements ISequentialInStream {

    public ISequentialInStream _stream;
    public long _size;
    public long _pos;
    public boolean _wasFinished;

    public void SetStream(ISequentialInStream stream) {
        _stream = stream;
    }

    public void Init(long streamSize) {
        _size = streamSize;
        _pos = 0;
        _wasFinished = false;
    }

    public HRESULT QueryInterface(long iid, byte[] outObject) {
//        *outObject = NULL;
//        if (iid == IID_IUnknown)
//            *outObject = ( void *)(IUnknown *) (ISequentialInStream *) this;
//        else if (iid == IID_ISequentialInStream)
//            *outObject = ( void *)(ISequentialInStream *) this;
//        else return E_NOINTERFACE;
//        ++__m_RefCount;
        return S_OK;
    }

    @Override
    public HRESULT Read(byte[] data, long size, long processedSize) {
        return null;
    }

    @Override
    public HRESULT QueryInterface(GUID iid, Object outObject) {
        return null;
    }

}
