package tangible.cpp.seven_zip.archive.zip;

import tangible.cpp.common.AString;
import tangible.cpp.common.CMyComBSTR;
import tangible.cpp.common.HRESULT;
import tangible.cpp.seven_zip.ICompressCoder;
import tangible.cpp.seven_zip.ICompressFilter;
import tangible.cpp.seven_zip.ICompressGetInStreamProcessedSize;
import tangible.cpp.seven_zip.ICompressProgressInfo;
import tangible.cpp.seven_zip.ICompressSetDecoderProperties2;
import tangible.cpp.seven_zip.ICompressSetFinishMode;
import tangible.cpp.seven_zip.ICryptoGetTextPassword;
import tangible.cpp.seven_zip.ICryptoSetPassword;
import tangible.cpp.seven_zip.ISequentialInStream;
import tangible.cpp.seven_zip.archive.IArchiveExtractCallback;
import tangible.cpp.seven_zip.archive.ISequentialOutStream;
import tangible.cpp.seven_zip.archive.common.CLimitedSequentialInStream;
import tangible.cpp.seven_zip.archive.common.COutStreamWithCRC;
import tangible.cpp.seven_zip.common.CFilterCoder;
import tangible.cpp.seven_zip.compress.NCompress;
import tangible.cpp.seven_zip.crypto.NCrypto;

import java.util.List;

import static tangible.cpp.common.HRESULT.E_FAIL;
import static tangible.cpp.common.HRESULT.E_NOTIMP;
import static tangible.cpp.common.HRESULT.S_FALSE;
import static tangible.cpp.common.HRESULT.S_OK;
import static tangible.cpp.seven_zip.ICompressGetInStreamProcessedSize.IID_ICompressGetInStreamProcessedSize;
import static tangible.cpp.seven_zip.ICompressSetDecoderProperties2.IID_ICompressSetDecoderProperties2;
import static tangible.cpp.seven_zip.ICompressSetFinishMode.IID_ICompressSetFinishMode;
import static tangible.cpp.seven_zip.ICryptoGetTextPassword.IID_ICryptoGetTextPassword;
import static tangible.cpp.seven_zip.ICryptoSetPassword.IID_ICryptoSetPassword;
import static tangible.cpp.seven_zip.archive.zip.NArchive.NZip.kMethodId_BZip2;
import static tangible.cpp.seven_zip.archive.zip.NArchive.NZip.kMethodId_ZipBase;

public class CZipDecoder {

    NCrypto.NZip.CDecoder _zipCryptoDecoderSpec;
    NCrypto.NZipStrong.CDecoder _pkAesDecoderSpec;
    NCrypto.NWzAes.CDecoder _wzAesDecoderSpec;

    ICompressFilter _zipCryptoDecoder;
    ICompressFilter _pkAesDecoder;
    ICompressFilter _wzAesDecoder;

    CFilterCoder filterStreamSpec;
    ISequentialInStream filterStream;
    ICryptoGetTextPassword[] getTextPassword = { null };
    List<CMethodItem> methodItems;

    CLzmaDecoder lzmaDecoderSpec;

    // ZipHandler.cpp:879
    public HRESULT Decode(CInArchive archive, CItemEx item,
            ISequentialOutStream realOutStream,
            IArchiveExtractCallback extractCallback,
            ICompressProgressInfo compressProgress,
            NExtract.NOperationResult[] res) {
        res[0] = NExtract.NOperationResult.kHeadersError;

        CFilterCoder.C_InStream_Releaser inStreamReleaser = null;
        CFilterCoder.C_Filter_Releaser filterReleaser = null;

        boolean needCRC = true;
        boolean wzAesMode = false;
        boolean pkAesMode = false;

        NFileHeader.NCompressionMethod.EType id = item.Method;

        if (item.IsEncrypted()) {
            if (item.IsStrongEncrypted()) {
                CStrongCryptoExtra f = new CStrongCryptoExtra();
                if (!item.CentralExtra.GetStrongCrypto(f)) {
                    res[0] = NExtract.NOperationResult.kUnsupportedMethod;
                    return S_OK;
                }
                pkAesMode = true;
            } else if (id == NFileHeader.NCompressionMethod.EType.kWzAES) {
                CWzAesExtra aesField = new CWzAesExtra();
                if (!item.GetMainExtra().GetWzAes(aesField))
                    return S_OK;
                wzAesMode = true;
                needCRC = aesField.NeedCrc();
            }
        }

        COutStreamWithCRC outStreamSpec = new COutStreamWithCRC();
        ISequentialOutStream outStream = outStreamSpec;
        outStreamSpec.SetStream(realOutStream);
        outStreamSpec.Init(needCRC);

        ISequentialInStream[] packStream = { null };

        CLimitedSequentialInStream limitedStreamSpec = new CLimitedSequentialInStream();
        ISequentialInStream inStream = limitedStreamSpec;

        {
            long packSize = item.PackSize;
            if (wzAesMode) {
                if (packSize < NCrypto.NWzAes.kMacSize)
                    return S_OK;
                packSize -= NCrypto.NWzAes.kMacSize;
            }

            HRESULT __result__ = archive.GetItemStream(item, true, packStream);
            if (__result__ != S_OK)
                return __result__;

            if (packStream[0] != null) {
                res[0] = NExtract.NOperationResult.kUnavailable;
                return S_OK;
            }
            limitedStreamSpec.SetStream(packStream[0]);
            limitedStreamSpec.Init(packSize);
        }


        res[0] = NExtract.NOperationResult.kDataError;

        ICompressFilter cryptoFilter = null;

        if (item.IsEncrypted()) {
            if (wzAesMode) {
                CWzAesExtra aesField = new CWzAesExtra();
                if (!item.GetMainExtra().GetWzAes(aesField))
                    return S_OK;
                id = aesField.Method;
                if (_wzAesDecoder == null) {
                    _wzAesDecoderSpec = new NCrypto.NWzAes.CDecoder();
                    _wzAesDecoder = _wzAesDecoderSpec;
                }
                cryptoFilter = _wzAesDecoder;
                if (!_wzAesDecoderSpec.SetKeyMode(aesField.Strength)) {
                    res[0] = NExtract.NOperationResult.kUnsupportedMethod;
                    return S_OK;
                }
            } else if (pkAesMode) {
                if (_pkAesDecoder == null) {
                    _pkAesDecoderSpec = new NCrypto.NZipStrong.CDecoder(0);
                    _pkAesDecoder = _pkAesDecoderSpec;
                }
                cryptoFilter = _pkAesDecoder;
            } else {
                if (_zipCryptoDecoder == null) {
                    _zipCryptoDecoderSpec = new NCrypto.NZip.CDecoder();
                    _zipCryptoDecoder = _zipCryptoDecoderSpec;
                }
                cryptoFilter = _zipCryptoDecoder;
            }

            ICryptoSetPassword[] cryptoSetPassword = { null };
            HRESULT __result__ = cryptoFilter.QueryInterface(IID_ICryptoSetPassword, cryptoSetPassword);
            if (__result__ != S_OK)
                return __result__;

            if (cryptoSetPassword[0] == null)
                return E_FAIL;

            if (getTextPassword[0] == null)
                extractCallback.QueryInterface(IID_ICryptoGetTextPassword, getTextPassword);

            if (getTextPassword[0] != null) {
                CMyComBSTR[] password = { null };

                __result__ = getTextPassword[0].CryptoGetTextPassword(password);
                if (__result__ != S_OK)
                    return __result__;

                AString[] charPassword = { null };
                if (password[0] != null) {
//                    UnicodeStringToMultiByte2(charPassword, (const wchar_t *)password, CP_ACP);
        /*
        if (wzAesMode || pkAesMode)
        {
        }
        else
        {
          // PASSWORD encoding for ZipCrypto:
          // pkzip25 / WinZip / Windows probably use ANSI
          // 7-Zip <  4.43 creates ZIP archives with OEM encoding in password
          // 7-Zip >= 4.43 creates ZIP archives only with ASCII characters in password
          // 7-Zip <  17.00 uses CP_OEMCP for password decoding
          // 7-Zip >= 17.00 uses CP_ACP   for password decoding
        }
        */
                }
                __result__ = cryptoSetPassword[0].CryptoSetPassword(charPassword[0], charPassword[0].Len());
                if (__result__ != S_OK) {
                    res[0] = NExtract.NOperationResult.kWrongPassword;
                    return S_OK;
                }
            } else {
                res[0] = NExtract.NOperationResult.kWrongPassword;
                return S_OK;
                // RINOK(cryptoSetPassword->CryptoSetPassword(NULL, 0));
            }
        }

        int m = 0;

        for (; m < methodItems.size(); m++)
            if (methodItems.get(m).ZipMethod == id)
                break;

        if (m == methodItems.size()) {
            CMethodItem mi = new CMethodItem();
            mi.ZipMethod = id;
            if (id == NFileHeader.NCompressionMethod.EType.kStore)
                mi.Coder = new NCompress.CCopyCoder();
            else if (id == NFileHeader.NCompressionMethod.EType.kShrink)
                mi.Coder = new NCompress.NShrink.CDecoder();
            else if (id == NFileHeader.NCompressionMethod.EType.kImplode)
                mi.Coder = new NCompress.NImplode.NDecoder.CCoder();
            else if (id == NFileHeader.NCompressionMethod.EType.kLZMA) {
//                lzmaDecoderSpec = new CLzmaDecoder;
//                mi.Coder = lzmaDecoderSpec;
            } else if (id == NFileHeader.NCompressionMethod.EType.kXz)
                mi.Coder = new NCompress.NXz.CComDecoder();
            else if (id == NFileHeader.NCompressionMethod.EType.kPPMd)
                mi.Coder = new NCompress.NPpmdZip.CDecoder(true);
            else {
                long szMethodID;
                if (id == NFileHeader.NCompressionMethod.EType.kBZip2)
                    szMethodID = kMethodId_BZip2;
                else {
                    if (id.ordinal() > 0xFF) {
                        res[0] = NExtract.NOperationResult.kUnsupportedMethod;
                        return S_OK;
                    }
                    szMethodID = kMethodId_ZipBase + id.ordinal();
                }

//                HRESULT __result__ = CreateCoder_Id(EXTERNAL_CODECS_LOC_VARS szMethodID, false, mi.Coder);
//                if (__result__ != S_OK) {
//                    res[0] = NOperationResult.kUnsupportedMethod;
//                    return S_OK;
//                }
            }

//            m = methodItems.add(mi);
        }

        ICompressCoder coder = methodItems.get(m).Coder;


//  #ifndef _7ZIP_ST
//        {
//            CMyComPtr<ICompressSetCoderMt> setCoderMt;
//            coder -> QueryInterface(IID_ICompressSetCoderMt, ( void **)&setCoderMt);
//            if (setCoderMt) {
//                RINOK(setCoderMt -> SetNumberOfThreads(numThreads));
//            }
//        }
//        // if (memUsage != 0)
//        {
//            CMyComPtr<ICompressSetMemLimit> setMemLimit;
//            coder -> QueryInterface(IID_ICompressSetMemLimit, ( void **)&setMemLimit);
//            if (setMemLimit) {
//                RINOK(setMemLimit -> SetMemLimit(memUsage));
//            }
//        }
//  #endif

        {
            ICompressSetDecoderProperties2[] setDecoderProperties = { null };
            coder.QueryInterface(IID_ICompressSetDecoderProperties2, setDecoderProperties);
            if (setDecoderProperties[0] != null) {
                int properties = item.Flags;

                HRESULT __result__ = setDecoderProperties[0].SetDecoderProperties2(properties, 1);
                if (__result__ == S_OK)
                    return __result__;
            }
        }


        ISequentialInStream inStreamNew = null;

        boolean isFullStreamExpected = !item.HasDescriptor() || item.PackSize != 0;
        boolean needReminderCheck = false;

        boolean dataAfterEnd = false;
        boolean truncatedError = false;
        boolean lzmaEosError = false;

        {
            HRESULT __result__ = S_OK;
            if (item.IsEncrypted()) {
                if (filterStream == null) {
                    filterStreamSpec = new CFilterCoder(false);
                    filterStream = filterStreamSpec;
                }

                filterReleaser.FilterCoder = filterStreamSpec;
                filterStreamSpec.Filter = cryptoFilter;

                if (wzAesMode) {
                    __result__ = _wzAesDecoderSpec.ReadHeader(inStream);
                    if (__result__ == S_OK) {
                        if (!_wzAesDecoderSpec.Init_and_CheckPassword()) {
                            res[0] = NExtract.NOperationResult.kWrongPassword;
                            return S_OK;
                        }
                    }
                } else if (pkAesMode) {
                    isFullStreamExpected = false;
                    __result__ = _pkAesDecoderSpec.ReadHeader(inStream, item.Crc, item.Size);
                    if (__result__ == S_OK) {
                        boolean[] passwOK = { true };
                        __result__ = _pkAesDecoderSpec.Init_and_CheckPassword(passwOK);
                        if (__result__ == S_OK && !passwOK[0]) {
                            res[0] = NExtract.NOperationResult.kWrongPassword;
                            return S_OK;
                        }
                    }
                } else {
                    __result__ = _zipCryptoDecoderSpec.ReadHeader(inStream);
                    if (__result__ == S_OK) {
                        _zipCryptoDecoderSpec.Init_BeforeDecode();

          /* Info-ZIP modification to ZipCrypto format:
               if bit 3 of the general purpose bit flag is set,
               it uses high byte of 16-bit File Time.
             Info-ZIP code probably writes 2 bytes of File Time.
             We check only 1 byte. */

                        // UInt32 v1 = GetUi16(_zipCryptoDecoderSpec->_header + NCrypto::NZip::kHeaderSize - 2);
                        // UInt32 v2 = (item.HasDescriptor() ? (item.Time & 0xFFFF) : (item.Crc >> 16));

                        int v1 = _zipCryptoDecoderSpec._header[NCrypto.NZip.kHeaderSize - 1];
                        int v2 = (int)(item.HasDescriptor() ? (item.Time >> 8) : (item.Crc >> 24));

                        if (v1 != v2) {
                            res[0] = NExtract.NOperationResult.kWrongPassword;
                            return S_OK;
                        }
                    }
                }

                if (__result__ == S_OK) {
                    inStreamReleaser.FilterCoder = filterStreamSpec;

                    __result__ = filterStreamSpec.SetInStream(inStream);
                    if (__result__ == S_OK)
                        return __result__;

        /* IFilter::Init() does nothing in all zip crypto filters.
           So we can call any Initialize function in CFilterCoder. */

                    __result__ = filterStreamSpec.Init_NoSubFilterInit();
                    if (__result__ == S_OK)
                        return __result__;

                    inStreamNew = filterStream;
                }
            } else
                inStreamNew = inStream;

            if (__result__ == S_OK) {
                ICompressSetFinishMode[] setFinishMode = { null };
                coder.QueryInterface(IID_ICompressSetFinishMode, setFinishMode);
                if (setFinishMode[0] != null) {
                    __result__ = setFinishMode[0].SetFinishMode(1);
                    if (__result__ == S_OK)
                        return __result__;
                }

                long coderPackSize = limitedStreamSpec.GetRem();

                boolean useUnpackLimit = (id == NFileHeader.NCompressionMethod.EType.kStore
                        || !item.HasDescriptor()
                        || item.Size[0] >= ((long)1 << 32)
                        || item.LocalExtra.IsZip64
                        || item.CentralExtra.IsZip64
                );

                __result__ = coder.Code(inStreamNew, outStream,
                        isFullStreamExpected ? coderPackSize : null,
                        // NULL,
                        useUnpackLimit ? item.Size : null,
                        compressProgress);

                if (__result__ == S_OK) {
                    ICompressGetInStreamProcessedSize[] getInStreamProcessedSize = { null };
                    coder.QueryInterface(IID_ICompressGetInStreamProcessedSize, getInStreamProcessedSize);
                    if (getInStreamProcessedSize[0] != null && setFinishMode[0] != null) {
                        long[] processed = { 0 };

                        __result__ = getInStreamProcessedSize[0].GetInStreamProcessedSize(processed);
                        if (__result__ == S_OK)
                            return __result__;

                        if (processed[0] != -1) {
                            if (pkAesMode) {
                                long padSize = _pkAesDecoderSpec.GetPadSize(processed[0]);
                                if (processed[0] + padSize > coderPackSize)
                                    truncatedError = true;
                                else {
                                    if (processed[0] + padSize < coderPackSize)
                                        dataAfterEnd = true;
                                    // also here we can check PKCS7 padding data from reminder (it can be inside stream buffer in coder).
                                }
                            } else {
                                if (processed[0] < coderPackSize) {
                                    if (isFullStreamExpected)
                                        dataAfterEnd = true;
                                } else if (processed[0] > coderPackSize)
                                    truncatedError = true;
                                needReminderCheck = isFullStreamExpected;
                            }
                        }
                    }
                }

                if (__result__ == S_OK && id == NFileHeader.NCompressionMethod.EType.kLZMA)
                    if (!lzmaDecoderSpec.DecoderSpec.CheckFinishStatus(item.IsLzmaEOS()))
                        lzmaEosError = true;
            }

            if (__result__ == S_FALSE)
                return S_OK;

            if (__result__ == E_NOTIMP) {
                res[0] = NExtract.NOperationResult.kUnsupportedMethod;
                return S_OK;
            }
            if (__result__ == S_OK)
                return S_OK;
        }

        boolean crcOK = true;
        boolean[] authOk = { true };
        if (needCRC)
            crcOK = outStreamSpec.GetCRC() == item.Crc;

        if (wzAesMode) {
            boolean[] thereAreData = { false };
            if (SkipStreamData(inStreamNew, thereAreData) != S_OK)
                authOk[0] = false;

            if (needReminderCheck && thereAreData[0])
                dataAfterEnd = true;

            limitedStreamSpec.Init(NCrypto.NWzAes.kMacSize);
            if (_wzAesDecoderSpec.CheckMac(inStream, authOk) != S_OK)
                authOk[0] = false;
        }

        res[0] = NExtract.NOperationResult.kCRCError;

        if (crcOK && authOk[0]) {
            res[0] = NExtract.NOperationResult.kOK;

            if (dataAfterEnd)
                res[0] = NExtract.NOperationResult.kDataAfterEnd;
            else if (truncatedError)
                res[0] = NExtract.NOperationResult.kUnexpectedEnd;
            else if (lzmaEosError)
                res[0] = NExtract.NOperationResult.kHeadersError;

            // CheckDescriptor() supports only data descriptor with signature and
            // it doesn't support "old" pkzip's data descriptor without signature.
            // So we disable that check.
    /*
    if (item.HasDescriptor() && archive.CheckDescriptor(item) != S_OK)
      res = NExtract::NOperationResult::kHeadersError;
    */
        }

        return S_OK;
    }

    public static HRESULT SkipStreamData(ISequentialInStream stream, boolean[] thereAreData) {
//        thereAreData = false;
//  const size_t kBufSize = 1 << 12;
//        Byte buf[ kBufSize];
//        for (; ; ) {
//            size_t size = kBufSize;
//            RINOK(ReadStream(stream, buf, & size));
//            if (size == 0)
//                return S_OK;
//            thereAreData = true;
//        }

        return S_OK;
    }
}
