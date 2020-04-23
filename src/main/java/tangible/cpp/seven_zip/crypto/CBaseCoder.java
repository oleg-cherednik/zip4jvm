package tangible.cpp.seven_zip.crypto;

import tangible.cpp.seven_zip.ICompressFilter;

// WzAes.h:80
public abstract class CBaseCoder implements ICompressFilter {

    public CKeyInfo _key;


    public boolean SetKeyMode(EKeySizeMode mode) {
        _key.KeySizeMode = mode;
        return true;
    }
}
