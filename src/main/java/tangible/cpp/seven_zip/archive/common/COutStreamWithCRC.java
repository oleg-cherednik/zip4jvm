package tangible.cpp.seven_zip.archive.common;

import tangible.cpp.common.GUID;
import tangible.cpp.common.HRESULT;
import tangible.cpp.seven_zip.archive.ISequentialOutStream;

// OutStreamWithCRC.h:12
public class COutStreamWithCRC implements ISequentialOutStream {

    public static final long CRC_INIT_VAL = 0xFFFFFFFF;

    public ISequentialOutStream _stream;
    public long _size;
    public boolean _calculate;
    public long _crc;

    public void SetStream(ISequentialOutStream stream) {
        _stream = stream;
    }

    public void Init(boolean calculate) {
        _size = 0;
        _calculate = calculate;
        _crc = CRC_INIT_VAL;
    }

    @Override
    public HRESULT QueryInterface(GUID iid, Object outObject) {
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
}
