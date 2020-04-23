package tangible.cpp.seven_zip.crypto;

import tangible.cpp.common.AString;
import tangible.cpp.common.CAlignedBuffer;
import tangible.cpp.common.CMyUnknownImp;
import tangible.cpp.common.GUID;
import tangible.cpp.common.HRESULT;
import tangible.cpp.seven_zip.ICompressFilter;
import tangible.cpp.seven_zip.ICryptoSetPassword;
import tangible.cpp.seven_zip.ISequentialInStream;
import tangible.cpp.seven_zip.archive.ISequentialOutStream;

import static tangible.c.Aes.AES_BLOCK_SIZE;
import static tangible.cpp.common.HRESULT.S_OK;

// ZipCrypto.h:11
public class NCrypto {

    // MyAes.h:14
    public static class CAesCbcCoder implements ICompressFilter {

        public CAesCbcCoder(boolean encodeMode, int keySize) {

        }

        @Override
        public HRESULT Init() {
            return null;
        }

        @Override
        public long Filter(byte[] data, long size) {
            return 0;
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
    }

    // MyAes.h:50
    public static class CAesCbcDecoder extends CAesCbcCoder {

        public CAesCbcDecoder(int keySize) {
            super(false, keySize);

        }
    }

    public static class NZip {

        public static final int kHeaderSize = 12;

        public abstract static class CCipher extends CMyUnknownImp implements ICompressFilter, ICryptoSetPassword {

            public long Key0;
            public long Key1;
            public long Key2;

            public long KeyMem0;
            public long KeyMem1;
            public long KeyMem2;

            void RestoreKeys() {
                Key0 = KeyMem0;
                Key1 = KeyMem1;
                Key2 = KeyMem2;
            }

//            MY_UNKNOWN_IMP1(ICryptoSetPassword)

            public HRESULT Init() {
                return S_OK;
            }

            public HRESULT CryptoSetPassword(char[] data, long size) {
                return S_OK;
            }

            @Override
            public HRESULT ResetInitVector() {
                return null;
            }
        }

        public static class CEncoder extends CCipher {

            public long Filter(byte[] data, long size) {
                return 0;
            }

            public HRESULT WriteHeader_Check16(ISequentialOutStream outStream, int crc) {
                return S_OK;
            }

            @Override
            public HRESULT QueryInterface(GUID iid, Object[] outObject) {
                return null;
            }

            @Override
            public HRESULT CryptoSetPassword(AString data, long size) {
                return null;
            }
        }

        public static class CDecoder extends CCipher {

            public byte[] _header = new byte[kHeaderSize];

            public long Filter(byte[] data, long size) {
                return 0;
            }

            public HRESULT ReadHeader(ISequentialInStream inStream) {
                return S_OK;
            }

            public void Init_BeforeDecode() {

            }

            @Override
            public HRESULT QueryInterface(GUID iid, Object[] outObject) {
                return null;
            }

            @Override
            public HRESULT CryptoSetPassword(AString data, long size) {
                return null;
            }
        }
    }

    public static class NWzAes {

        public static final long kPasswordSizeMax = 99; // 128;
        public static final long kSaltSizeMax = 16;
        public static final long kPwdVerifSize = 2;
        public static final long kMacSize = 10;

        // WzAes.h:121
        public static class CDecoder extends CBaseCoder {

            @Override
            public HRESULT Init() {
                return null;
            }

            @Override
            public long Filter(byte[] data, long size) {
                return 0;
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

            // ZipString.h:68
            public HRESULT ReadHeader(ISequentialInStream inStream, long crc, long unpackSize) {
                return S_OK;
            }

            // WzAes.cpp:122
            public HRESULT ReadHeader(ISequentialInStream inStream) {
//                unsigned saltSize = _key.GetSaltSize();
//                unsigned extraSize = saltSize + kPwdVerifSize;
//                Byte temp[kSaltSizeMax + kPwdVerifSize];
//                RINOK(ReadStream_FAIL(inStream, temp, extraSize));
//                unsigned i;
//                for (i = 0; i < saltSize; i++)
//                    _key.Salt[i] = temp[i];
//                for (i = 0; i < kPwdVerifSize; i++)
//                    _pwdVerifFromArchive[i] = temp[saltSize + i];
                return S_OK;
            }

            // WzAes.cpp:143
            public boolean Init_and_CheckPassword() {
//                Init2();
//                return CompareArrays(_key.PwdVerifComputed, _pwdVerifFromArchive, kPwdVerifSize);
                return false;
            }

            // WzAes.cpp:150
            public HRESULT CheckMac(ISequentialInStream inStream, boolean[] isOK) {
//                isOK = false;
//                Byte mac1[ kMacSize];
//                RINOK(ReadStream_FAIL(inStream, mac1, kMacSize));
//                Byte mac2[ kMacSize];
//                _hmac.Final(mac2, kMacSize);
//                isOK = CompareArrays(mac1, mac2, kMacSize);
                return S_OK;
            }
        }
    }

    // ZipString.h:24
    public static class NZipStrong {

        public static final int kAesPadAllign = AES_BLOCK_SIZE;

        public static class CKeyInfo {

            public byte[] MasterKey = new byte[32];
            public long KeySize;

            public void SetPassword(char[] data, long size) {

            }
        }

        // ZipString.h:32
        public static class CBaseCoder extends CAesCbcDecoder implements ICryptoSetPassword {

            public CKeyInfo _key;
            public CAlignedBuffer _bufAligned;

            public CBaseCoder(int keySize) {
                super(keySize);
            }

            public HRESULT Init() {
                return S_OK;
            }

            @Override
            public HRESULT CryptoSetPassword(AString data, long size) {
                return S_OK;
            }

            @Override
            public HRESULT ResetInitVector() {
                return null;
            }

            @Override
            public HRESULT QueryInterface(GUID iid, Object[] outObject) {
                return null;
            }
        }

        // ZipString.h:46
        public static class CDecoder extends CBaseCoder {

            public long _ivSize;
            public byte[] _iv = new byte[16];
            public long _remSize;

            public CDecoder(int keySize) {
                super(keySize);
            }

//            MY_UNKNOWN_IMP1(ICryptoSetPassword)

            public HRESULT ReadHeader(ISequentialInStream inStream, long crc, long[] unpackSize) {
                return S_OK;
            }

            public HRESULT Init_and_CheckPassword(boolean[] passwOK) {
                return S_OK;
            }

            public long GetPadSize(long packSize32) {
                // Padding is to align to blockSize of cipher.
                // Change it, if is not AES
                return kAesPadAllign - (packSize32 & (kAesPadAllign - 1));
            }
        }
    }


}
