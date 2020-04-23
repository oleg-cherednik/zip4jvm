package tangible.cpp.seven_zip.archive.zip;

import tangible.cpp.common.CMyUnknownImp;
import tangible.cpp.common.GUID;
import tangible.cpp.common.HRESULT;
import tangible.cpp.common.MyWindows;
import tangible.cpp.seven_zip.ICompressProgressInfo;
import tangible.cpp.seven_zip.archive.IArchiveExtractCallback;
import tangible.cpp.seven_zip.archive.IArchiveOpenCallback;
import tangible.cpp.seven_zip.archive.IArchiveUpdateCallback;
import tangible.cpp.seven_zip.archive.ICompressCodecsInfo;
import tangible.cpp.seven_zip.archive.IInArchive;
import tangible.cpp.seven_zip.archive.IOutArchive;
import tangible.cpp.seven_zip.archive.ISequentialOutStream;
import tangible.cpp.seven_zip.archive.ISetProperties;
import tangible.cpp.seven_zip.archive.PROPVARIANT;
import tangible.cpp.seven_zip.common.CExternalCodecs;
import tangible.cpp.seven_zip.common.CLocalProgress;

import java.io.InputStream;

import static tangible.cpp.common.HRESULT.S_FALSE;
import static tangible.cpp.common.HRESULT.S_OK;

public class CHandler extends CMyUnknownImp implements IInArchive, IOutArchive, ISetProperties, ICompressCodecsInfo {

    CItemEx[] m_Items;
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
        return S_OK;
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

    // ZipHandler.cpp:1342
    @Override
    public HRESULT Extract(int[] indices, long numItems, boolean testMode, IArchiveExtractCallback extractCallback) {
        CZipDecoder myDecoder = null;
        long totalUnPacked = 0, totalPacked = 0;
        boolean allFilesMode = numItems == -1;
        if (allFilesMode)
            numItems = m_Items.length;
        if (numItems == 0)
            return S_OK;

        for (int i = 0; i < numItems; i++) {
            CItemEx item = m_Items[allFilesMode ? i : indices[i]];
            totalUnPacked += item.Size;
            totalPacked += item.PackSize;
        }

        HRESULT __result__ = extractCallback.SetTotal(totalUnPacked);
        if (__result__ != S_OK)
            return __result__;

        long currentTotalUnPacked = 0, currentTotalPacked = 0;
        long currentItemUnPacked, currentItemPacked;

        CLocalProgress lps = new CLocalProgress();
        ICompressProgressInfo progress = lps;
        lps.Init(extractCallback, false);

        for (int i = 0; i < numItems; i++,
                currentTotalUnPacked += currentItemUnPacked,
                currentTotalPacked += currentItemPacked) {
            currentItemUnPacked = 0;
            currentItemPacked = 0;

            lps.InSize = currentTotalPacked;
            lps.OutSize = currentTotalUnPacked;

            __result__ = lps.SetCur();
            if (__result__ != S_OK)
                return __result__;

            ISequentialOutStream[] realOutStream = new ISequentialOutStream[1];
            NAskMode askMode = testMode ? NAskMode.kTest : NAskMode.kExtract;
            int index = allFilesMode ? i : indices[i];

            CItemEx item = m_Items[index];
            boolean isLocalOffsetOK = m_Archive.IsLocalOffsetOK(item);
            boolean skip = !isLocalOffsetOK && !item.IsDir();

            if (skip)
                askMode = NAskMode.kSkip;

            currentItemUnPacked = item.Size;
            currentItemPacked = item.PackSize;

            __result__ = extractCallback.GetStream(index, realOutStream, askMode);
            if (__result__ != S_OK)
                return __result__;

            if (!isLocalOffsetOK) {
                __result__ = extractCallback.PrepareOperation(askMode);
                if (__result__ != S_OK)
                    return __result__;

                realOutStream[0].Release();

                __result__ = extractCallback.SetOperationResult(NOperationResult.kUnavailable);
                if (__result__ != S_OK)
                    return __result__;

                continue;
            }

            boolean[] headersError = { false };

            if (!item.FromLocal) {
                boolean[] isAvail = { true };
                HRESULT res = m_Archive.ReadLocalItemAfterCdItem(item, isAvail, headersError);
                if (res == S_FALSE) {
                    if (item.IsDir() || realOutStream[0] != null || testMode) {
                        __result__ = extractCallback.PrepareOperation(askMode);
                        if (__result__ != S_OK)
                            return __result__;

                        realOutStream[0].Release();

                        __result__ = extractCallback.SetOperationResult(isAvail[0] ? NOperationResult.kHeadersError : NOperationResult.kUnavailable);
                        if (__result__ != S_OK)
                            return __result__;
                    }

                    continue;
                }

                if (res != S_OK)
                    return res;
            }

            if (item.IsDir()) {
                // if (!testMode)
                {
                    __result__ = extractCallback.PrepareOperation(askMode);
                    if (__result__ != S_OK)
                        return __result__;

                    realOutStream[0].Release();

                    __result__ = extractCallback.SetOperationResult(NOperationResult.kOK);
                    if (__result__ != S_OK)
                        return __result__;
                }
                continue;
            }

            if (!testMode && realOutStream[0] == null)
                continue;

            __result__ = extractCallback.PrepareOperation(askMode);
            if (__result__ != S_OK)
                return __result__;


            NOperationResult[] res = { NOperationResult.kOK };
            HRESULT hres = myDecoder.Decode(
                    m_Archive, item, realOutStream, extractCallback,
                    progress,
                    res);

            __result__ = extractCallback.PrepareOperation(askMode);
            if (hres != S_OK)
                return hres;

            realOutStream[0].Release();

            if (res[0] == NOperationResult.kOK && headersError[0])
                res[0] = NOperationResult.kHeadersError;

            __result__ = extractCallback.SetOperationResult(res[0]);
            if (__result__ != S_OK)
                return __result__;
        }

        lps.InSize = currentTotalPacked;
        lps.OutSize = currentTotalUnPacked;
        return lps.SetCur();
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

    HRESULT QueryInterface(GUID iid, Object[] outObject) {
//         *outObject = NULL;
//
//        if (iid instanceof IUnknown . == IID_IUnknown)
//              *outObject = ( void *)(IUnknown *) (IInArchive *) this;
//         else if (iid == IID_IInArchive)
//             *outObject = ( void *)(IInArchive *) this;
//        else if (iid == IID_IOutArchive)
//            *outObject = ( void *)(IOutArchive *) this;
//        else if (iid == IID_ISetProperties)
//            *outObject = ( void *)(ISetProperties *) this;
//        else if (iid == IID_ ISetCompressCodecsInfo)
//            *outObject = ( void *)(ISetCompressCodecsInfo)this;
//        else
//        return E_NOINTERFACE;

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

    // ZipString.cpp:68
    HRESULT ReadHeader(InputStream inStream, long crc, long unpackSize) {
//        Byte temp[ 4];
//        RINOK(ReadStream_FALSE(inStream, temp, 2));
//        _ivSize = GetUi16(temp);
//        if (_ivSize == 0) {
//            memset(_iv, 0, 16);
//            SetUi32(_iv + 0, crc);
//            SetUi64(_iv + 4, unpackSize);
//            _ivSize = 12;
//        } else if (_ivSize == 16) {
//            RINOK(ReadStream_FALSE(inStream, _iv, _ivSize));
//        } else
//            return E_NOTIMPL;
//        RINOK(ReadStream_FALSE(inStream, temp, 4));
//        _remSize = GetUi32(temp);
//        // const UInt32 kAlign = 16;
//        if (_remSize < 16 || _remSize > (1 << 18))
//            return E_NOTIMPL;
//        if (_remSize > _bufAligned.Size()) {
//            _bufAligned.AllocAtLeast(_remSize);
//            if (!(Byte *) _bufAligned)
//            return E_OUTOFMEMORY;
//        }
//        return ReadStream_FALSE(inStream, _bufAligned, _remSize);

        return S_OK;
    }


}
