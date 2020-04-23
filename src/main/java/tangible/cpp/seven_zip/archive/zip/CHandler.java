package tangible.cpp.seven_zip.archive.zip;

import tangible.cpp.common.CMyUnknownImp;
import tangible.cpp.common.GUID;
import tangible.cpp.common.HRESULT;
import tangible.cpp.common.IUnknown;
import tangible.cpp.common.MyWindows;
import tangible.cpp.seven_zip.archive.IArchiveExtractCallback;
import tangible.cpp.seven_zip.archive.IArchiveOpenCallback;
import tangible.cpp.seven_zip.archive.IArchiveUpdateCallback;
import tangible.cpp.seven_zip.archive.ICompressCodecsInfo;
import tangible.cpp.seven_zip.archive.IInArchive;
import tangible.cpp.seven_zip.archive.IOutArchive;
import tangible.cpp.seven_zip.archive.ISequentialOutStream;
import tangible.cpp.seven_zip.archive.ISetCompressCodecsInfo;
import tangible.cpp.seven_zip.archive.ISetProperties;
import tangible.cpp.seven_zip.archive.PROPVARIANT;
import tangible.cpp.seven_zip.common.CExternalCodecs;

import java.io.InputStream;
import java.util.List;

import static tangible.cpp.common.HRESULT.E_NOINTERFACE;
import static tangible.cpp.common.HRESULT.S_OK;

public class CHandler extends CMyUnknownImp implements IInArchive, IOutArchive, ISetProperties, ICompressCodecsInfo {

    List<CItemEx> m_Items;
    CInArchive m_Archive;

    final CBaseProps _props = new CBaseProps();

    int m_MainMethod;
    boolean m_ForceAesMode;
    boolean m_WriteNtfsTimeExtra;
    boolean _removeSfxBlock;
    boolean m_ForceLocal;
    boolean m_ForceUtf8;
    boolean _forceCodePage;
    long _specifiedCodePage;
    CExternalCodecs __externalCodecs;

    void InitMethodProps() {
        _props.Init();
        m_MainMethod = -1;
        m_ForceAesMode = false;
        m_WriteNtfsTimeExtra = true;
        _removeSfxBlock = false;
        m_ForceLocal = false;
        m_ForceUtf8 = false;
        _forceCodePage = false;
        _specifiedCodePage = MyWindows.CP_OEMCP;
    }

    // void MarkAltStreams(CObjectVector<CItemEx> &items);

    HRESULT GetOutProperty(IArchiveUpdateCallback callback, long callbackIndex, int arcIndex, long propID, PROPVARIANT value) {

    }

    // public
    @Override
    public HRESULT GetNumMethods(long[] numMethods) {
        return null;
    }

    @Override
    public HRESULT GetProperty(long[] index, long propID, PROPVARIANT value) {
        return null;
    }

    @Override
    public HRESULT CreateDecoder(long index, GUID iid, Object coder) {
        return null;
    }

    @Override
    public HRESULT CreateEncoder(long index, GUID iid, Object coder) {
        return null;
    }

    @Override
    public HRESULT Open(InputStream stream, long[] maxCheckStartPosition, IArchiveOpenCallback openCallback) {
        return null;
    }

    @Override
    public HRESULT Close() {
        return null;
    }

    @Override
    public HRESULT GetNumberOfItems(long[] numItems) {
        return null;
    }

    @Override
    public HRESULT GetProperty(long index, long propID, PROPVARIANT value) {
        return null;
    }

    @Override
    public HRESULT Extract(long[] indices, long numItems, int testMode, IArchiveExtractCallback extractCallback) {
        return null;
    }

    @Override
    public HRESULT GetArchiveProperty(long propID, PROPVARIANT value) {
        return null;
    }

    @Override
    public HRESULT GetNumberOfProperties(long[] numProps) {
        return null;
    }

    @Override
    public HRESULT GetPropertyInfo(long index, char[] name, long[] propID, int[] varType) {
        return null;
    }

    @Override
    public HRESULT GetNumberOfArchiveProperties(long[] numProps) {
        return null;
    }

    @Override
    public HRESULT GetArchivePropertyInfo(long index, char[] name, long[] propID, int[] varType) {
        return null;
    }

    @Override
    public HRESULT UpdateItems(ISequentialOutStream outStream, long numItems, IArchiveUpdateCallback updateCallback) {
        return null;
    }

    @Override
    public HRESULT GetFileTimeType(long[] type) {
        return null;
    }

    @Override
    public HRESULT SetProperties(char[] names, PROPVARIANT values, long numProps) {
        return null;
    }

    @Override
    public HRESULT QueryInterface(GUID iid, Object outObject) {
        return null;
    }

    @Override
    public long AddRef() {
        return ++__m_RefCount;
    }

    @Override
    public long Release() {
        return --__m_RefCount;
    }

    // public

    HRESULT QueryInterface(GUID iid, void **outObject) {
         *outObject = NULL;

        if (iid instanceof IUnknown . == IID_IUnknown)
              *outObject = ( void *)(IUnknown *) (IInArchive *) this;
         else if (iid == IID_IInArchive)
             *outObject = ( void *)(IInArchive *) this;
        else if (iid == IID_IOutArchive)
            *outObject = ( void *)(IOutArchive *) this;
        else if (iid == IID_ISetProperties)
            *outObject = ( void *)(ISetProperties *) this;
        else if (iid == IID_ ISetCompressCodecsInfo)
            *outObject = ( void *)(ISetCompressCodecsInfo)this;
        else
        return E_NOINTERFACE;

        ++__m_RefCount;
        return S_OK;
    }

    public static final String kMethod_AES = "AES";
    public static final String kMethod_ZipCrypto = "ZipCrypto";
    public static final String kMethod_StrongCrypto = "StrongCrypto";

    // ZipHandler.cpp: 42
    private static final String[] kHostOS = {};
    // ZipHandler.cpp: 67
    private static final String[] c = {};
    // ZipHandler.cpp: 87
    private static final String[] kMethodNames2 = {};

    public static final CIdToNamePair[] k_StrongCryptoPairs = {
            new CIdToNamePair(NStrongCrypto_AlgId.kDES, "DES"),
            new CIdToNamePair(NStrongCrypto_AlgId.kRC2old, "RC2a"),
            new CIdToNamePair(NStrongCrypto_AlgId.k3DES168, "3DES-168"),
            new CIdToNamePair(NStrongCrypto_AlgId.k3DES112, "3DES-112"),
            new CIdToNamePair(NStrongCrypto_AlgId.kAES128, "pkAES-128"),
            new CIdToNamePair(NStrongCrypto_AlgId.kAES192, "pkAES-192"),
            new CIdToNamePair(NStrongCrypto_AlgId.kAES256, "pkAES-256"),
            new CIdToNamePair(NStrongCrypto_AlgId.kRC2, "RC2"),
            new CIdToNamePair(NStrongCrypto_AlgId.kBlowfish, "Blowfish"),
            new CIdToNamePair(NStrongCrypto_AlgId.kTwofish, "Twofish"),
            new CIdToNamePair(NStrongCrypto_AlgId.kRC4, "RC4") };
}


}
