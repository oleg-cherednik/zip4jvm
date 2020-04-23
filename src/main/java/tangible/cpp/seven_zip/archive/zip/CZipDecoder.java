package tangible.cpp.seven_zip.archive.zip;

import tangible.cpp.common.HRESULT;
import tangible.cpp.seven_zip.ICompressProgressInfo;
import tangible.cpp.seven_zip.archive.IArchiveExtractCallback;
import tangible.cpp.seven_zip.archive.ISequentialOutStream;

public class CZipDecoder {

    // ZipHandler.cpp:879
    public HRESULT Decode(CInArchive archive, CItemEx item,
            ISequentialOutStream[] realOutStream,
            IArchiveExtractCallback extractCallback,
            ICompressProgressInfo compressProgress,
            NOperationResult[] res) {
        res[0] = NOperationResult.kHeadersError;

        CFilterCoder::C_InStream_Releaser inStreamReleaser;
        CFilterCoder::C_Filter_Releaser filterReleaser;

        bool needCRC = true;
        bool wzAesMode = false;
        bool pkAesMode = false;

        unsigned id = item.Method;

        if (item.IsEncrypted()) {
            if (item.IsStrongEncrypted()) {
                CStrongCryptoExtra f;
                if (!item.CentralExtra.GetStrongCrypto(f)) {
                    res = NExtract::NOperationResult::kUnsupportedMethod;
                    return S_OK;
                }
                pkAesMode = true;
            } else if (id == NFileHeader::NCompressionMethod::kWzAES)
            {
                CWzAesExtra aesField;
                if (!item.GetMainExtra().GetWzAes(aesField))
                    return S_OK;
                wzAesMode = true;
                needCRC = aesField.NeedCrc();
            }
        }

        COutStreamWithCRC * outStreamSpec = new COutStreamWithCRC;
        CMyComPtr<ISequentialOutStream> outStream = outStreamSpec;
        outStreamSpec -> SetStream(realOutStream);
        outStreamSpec -> Init(needCRC);

        CMyComPtr<ISequentialInStream> packStream;

        CLimitedSequentialInStream * limitedStreamSpec = new CLimitedSequentialInStream;
        CMyComPtr<ISequentialInStream> inStream (limitedStreamSpec);

        {
            UInt64 packSize = item.PackSize;
            if (wzAesMode) {
                if (packSize < NCrypto::NWzAes::kMacSize)
                return S_OK;
                packSize -= NCrypto::NWzAes::kMacSize;
            }
            RINOK(archive.GetItemStream(item, true, packStream));
            if (!packStream) {
                res = NExtract::NOperationResult::kUnavailable;
                return S_OK;
            }
            limitedStreamSpec -> SetStream(packStream);
            limitedStreamSpec -> Init(packSize);
        }


        res = NExtract::NOperationResult::kDataError;

        CMyComPtr<ICompressFilter> cryptoFilter;

        if (item.IsEncrypted()) {
            if (wzAesMode) {
                CWzAesExtra aesField;
                if (!item.GetMainExtra().GetWzAes(aesField))
                    return S_OK;
                id = aesField.Method;
                if (!_wzAesDecoder) {
                    _wzAesDecoderSpec = new NCrypto::NWzAes::CDecoder;
                    _wzAesDecoder = _wzAesDecoderSpec;
                }
                cryptoFilter = _wzAesDecoder;
                if (!_wzAesDecoderSpec -> SetKeyMode(aesField.Strength)) {
                    res = NExtract::NOperationResult::kUnsupportedMethod;
                    return S_OK;
                }
            } else if (pkAesMode) {
                if (!_pkAesDecoder) {
                    _pkAesDecoderSpec = new NCrypto::NZipStrong::CDecoder;
                    _pkAesDecoder = _pkAesDecoderSpec;
                }
                cryptoFilter = _pkAesDecoder;
            } else {
                if (!_zipCryptoDecoder) {
                    _zipCryptoDecoderSpec = new NCrypto::NZip::CDecoder;
                    _zipCryptoDecoder = _zipCryptoDecoderSpec;
                }
                cryptoFilter = _zipCryptoDecoder;
            }

            CMyComPtr<ICryptoSetPassword> cryptoSetPassword;
            RINOK(cryptoFilter.QueryInterface(IID_ICryptoSetPassword, & cryptoSetPassword));
            if (!cryptoSetPassword)
                return E_FAIL;

            if (!getTextPassword)
                extractCallback -> QueryInterface(IID_ICryptoGetTextPassword, ( void **)&getTextPassword);

            if (getTextPassword) {
                CMyComBSTR password;
                RINOK(getTextPassword -> CryptoGetTextPassword( & password));
                AString charPassword;
                if (password) {
                    UnicodeStringToMultiByte2(charPassword, (const wchar_t *)password, CP_ACP);
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
                HRESULT result = cryptoSetPassword -> CryptoSetPassword(
                        (const Byte *)(const char *)charPassword, charPassword.Len());
                if (result != S_OK) {
                    res = NExtract::NOperationResult::kWrongPassword;
                    return S_OK;
                }
            } else {
                res = NExtract::NOperationResult::kWrongPassword;
                return S_OK;
                // RINOK(cryptoSetPassword->CryptoSetPassword(NULL, 0));
            }
        }

        unsigned m;
        for (m = 0; m < methodItems.Size(); m++)
            if (methodItems[m].ZipMethod == id)
                break;

        if (m == methodItems.Size()) {
            CMethodItem mi;
            mi.ZipMethod = id;
            if (id == NFileHeader::NCompressionMethod::kStore)
            mi.Coder = new NCompress::CCopyCoder;
    else if (id == NFileHeader::NCompressionMethod::kShrink)
            mi.Coder = new NCompress::NShrink::CDecoder;
    else if (id == NFileHeader::NCompressionMethod::kImplode)
            mi.Coder = new NCompress::NImplode::NDecoder::CCoder;
    else if (id == NFileHeader::NCompressionMethod::kLZMA)
            {
                lzmaDecoderSpec = new CLzmaDecoder;
                mi.Coder = lzmaDecoderSpec;
            }
    else if (id == NFileHeader::NCompressionMethod::kXz)
            mi.Coder = new NCompress::NXz::CComDecoder;
    else if (id == NFileHeader::NCompressionMethod::kPPMd)
            mi.Coder = new NCompress::NPpmdZip::CDecoder(true);
    else
            {
                CMethodId szMethodID;
                if (id == NFileHeader::NCompressionMethod::kBZip2)
                szMethodID = kMethodId_BZip2;
      else
                {
                    if (id > 0xFF) {
                        res = NExtract::NOperationResult::kUnsupportedMethod;
                        return S_OK;
                    }
                    szMethodID = kMethodId_ZipBase + (Byte)id;
                }

                RINOK(CreateCoder_Id(EXTERNAL_CODECS_LOC_VARS szMethodID, false, mi.Coder));

                if (!mi.Coder) {
                    res = NExtract::NOperationResult::kUnsupportedMethod;
                    return S_OK;
                }
            }
            m = methodItems.Add(mi);
        }

        ICompressCoder * coder = methodItems[m].Coder;


  #ifndef _7ZIP_ST
        {
            CMyComPtr<ICompressSetCoderMt> setCoderMt;
            coder -> QueryInterface(IID_ICompressSetCoderMt, ( void **)&setCoderMt);
            if (setCoderMt) {
                RINOK(setCoderMt -> SetNumberOfThreads(numThreads));
            }
        }
        // if (memUsage != 0)
        {
            CMyComPtr<ICompressSetMemLimit> setMemLimit;
            coder -> QueryInterface(IID_ICompressSetMemLimit, ( void **)&setMemLimit);
            if (setMemLimit) {
                RINOK(setMemLimit -> SetMemLimit(memUsage));
            }
        }
  #endif

        {
            CMyComPtr<ICompressSetDecoderProperties2> setDecoderProperties;
            coder -> QueryInterface(IID_ICompressSetDecoderProperties2, ( void **)&setDecoderProperties);
            if (setDecoderProperties) {
                Byte properties = (Byte)item.Flags;
                RINOK(setDecoderProperties -> SetDecoderProperties2( & properties, 1));
            }
        }


        CMyComPtr<ISequentialInStream> inStreamNew;

        bool isFullStreamExpected = (!item.HasDescriptor() || item.PackSize != 0);
        bool needReminderCheck = false;

        bool dataAfterEnd = false;
        bool truncatedError = false;
        bool lzmaEosError = false;

        {
            HRESULT result = S_OK;
            if (item.IsEncrypted()) {
                if (!filterStream) {
                    filterStreamSpec = new CFilterCoder(false);
                    filterStream = filterStreamSpec;
                }

                filterReleaser.FilterCoder = filterStreamSpec;
                filterStreamSpec -> Filter = cryptoFilter;

                if (wzAesMode) {
                    result = _wzAesDecoderSpec -> ReadHeader(inStream);
                    if (result == S_OK) {
                        if (!_wzAesDecoderSpec -> Init_and_CheckPassword()) {
                            res = NExtract::NOperationResult::kWrongPassword;
                            return S_OK;
                        }
                    }
                } else if (pkAesMode) {
                    isFullStreamExpected = false;
                    result = _pkAesDecoderSpec -> ReadHeader(inStream, item.Crc, item.Size);
                    if (result == S_OK) {
                        bool passwOK;
                        result = _pkAesDecoderSpec -> Init_and_CheckPassword(passwOK);
                        if (result == S_OK && !passwOK) {
                            res = NExtract::NOperationResult::kWrongPassword;
                            return S_OK;
                        }
                    }
                } else {
                    result = _zipCryptoDecoderSpec -> ReadHeader(inStream);
                    if (result == S_OK) {
                        _zipCryptoDecoderSpec -> Init_BeforeDecode();

          /* Info-ZIP modification to ZipCrypto format:
               if bit 3 of the general purpose bit flag is set,
               it uses high byte of 16-bit File Time.
             Info-ZIP code probably writes 2 bytes of File Time.
             We check only 1 byte. */

                        // UInt32 v1 = GetUi16(_zipCryptoDecoderSpec->_header + NCrypto::NZip::kHeaderSize - 2);
                        // UInt32 v2 = (item.HasDescriptor() ? (item.Time & 0xFFFF) : (item.Crc >> 16));

                        Byte v1 = _zipCryptoDecoderSpec -> _header[NCrypto::NZip::kHeaderSize - 1];
                        Byte v2 = (Byte)(item.HasDescriptor() ? (item.Time >> 8) : (item.Crc >> 24));

                        if (v1 != v2) {
                            res = NExtract::NOperationResult::kWrongPassword;
                            return S_OK;
                        }
                    }
                }

                if (result == S_OK) {
                    inStreamReleaser.FilterCoder = filterStreamSpec;
                    RINOK(filterStreamSpec -> SetInStream(inStream));

        /* IFilter::Init() does nothing in all zip crypto filters.
           So we can call any Initialize function in CFilterCoder. */

                    RINOK(filterStreamSpec -> Init_NoSubFilterInit());
                    // RINOK(filterStreamSpec->SetOutStreamSize(NULL));

                    inStreamNew = filterStream;
                }
            } else
                inStreamNew = inStream;

            if (result == S_OK) {
                CMyComPtr<ICompressSetFinishMode> setFinishMode;
                coder -> QueryInterface(IID_ICompressSetFinishMode, ( void **)&setFinishMode);
                if (setFinishMode) {
                    RINOK(setFinishMode -> SetFinishMode(BoolToInt(true)));
                }

      const UInt64 coderPackSize = limitedStreamSpec -> GetRem();

                bool useUnpackLimit = (id == 0
                        || !item.HasDescriptor()
                        || item.Size >= ((UInt64)1 << 32)
                        || item.LocalExtra.IsZip64
                        || item.CentralExtra.IsZip64
                );

                result = coder -> Code(inStreamNew, outStream,
                        isFullStreamExpected ? & coderPackSize :NULL,
                        // NULL,
                        useUnpackLimit ? &item.Size :NULL,
                        compressProgress);

                if (result == S_OK) {
                    CMyComPtr<ICompressGetInStreamProcessedSize> getInStreamProcessedSize;
                    coder -> QueryInterface(IID_ICompressGetInStreamProcessedSize, ( void **)&getInStreamProcessedSize);
                    if (getInStreamProcessedSize && setFinishMode) {
                        UInt64 processed;
                        RINOK(getInStreamProcessedSize -> GetInStreamProcessedSize( & processed));
                        if (processed != (UInt64)(Int64) - 1) {
                            if (pkAesMode) {
              const UInt32 padSize = _pkAesDecoderSpec -> GetPadSize((UInt32)processed);
                                if (processed + padSize > coderPackSize)
                                    truncatedError = true;
                                else {
                                    if (processed + padSize < coderPackSize)
                                        dataAfterEnd = true;
                                    // also here we can check PKCS7 padding data from reminder (it can be inside stream buffer in coder).
                                }
                            } else {
                                if (processed < coderPackSize) {
                                    if (isFullStreamExpected)
                                        dataAfterEnd = true;
                                } else if (processed > coderPackSize)
                                    truncatedError = true;
                                needReminderCheck = isFullStreamExpected;
                            }
                        }
                    }
                }

                if (result == S_OK && id == NFileHeader::NCompressionMethod::kLZMA)
                if (!lzmaDecoderSpec -> DecoderSpec -> CheckFinishStatus(item.IsLzmaEOS()))
                    lzmaEosError = true;
            }

            if (result == S_FALSE)
                return S_OK;

            if (result == E_NOTIMPL) {
                res = NExtract::NOperationResult::kUnsupportedMethod;
                return S_OK;
            }

            RINOK(result);
        }

        bool crcOK = true;
        bool authOk = true;
        if (needCRC)
            crcOK = (outStreamSpec -> GetCRC() == item.Crc);

        if (wzAesMode) {
            bool thereAreData = false;
            if (SkipStreamData(inStreamNew, thereAreData) != S_OK)
                authOk = false;

            if (needReminderCheck && thereAreData)
                dataAfterEnd = true;

            limitedStreamSpec -> Init(NCrypto::NWzAes::kMacSize);
            if (_wzAesDecoderSpec -> CheckMac(inStream, authOk) != S_OK)
                authOk = false;
        }

        res = NExtract::NOperationResult::kCRCError;

        if (crcOK && authOk) {
            res = NExtract::NOperationResult::kOK;

            if (dataAfterEnd)
                res = NExtract::NOperationResult::kDataAfterEnd;
    else if (truncatedError)
                res = NExtract::NOperationResult::kUnexpectedEnd;
    else if (lzmaEosError)
                res = NExtract::NOperationResult::kHeadersError;

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
}
