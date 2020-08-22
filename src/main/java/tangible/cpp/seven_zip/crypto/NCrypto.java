package tangible.cpp.seven_zip.crypto;

import tangible.c.Aes;
import tangible.cpp.common.CMyUnknownImp;
import tangible.cpp.common.GUID;
import tangible.cpp.common.HRESULT;
import tangible.cpp.seven_zip.ICompressFilter;
import tangible.cpp.seven_zip.ICryptoProperties;
import tangible.cpp.seven_zip.ICryptoSetPassword;
import tangible.cpp.seven_zip.ISequentialInStream;
import tangible.cpp.seven_zip.archive.ISequentialOutStream;

import java.security.MessageDigest;
import java.util.Arrays;

import static tangible.c.Aes.AES_BLOCK_SIZE;
import static tangible.c.Aes.AES_NUM_IVMRK_WORDS;
import static tangible.c.Aes.AesCbc_Init;
import static tangible.c.Sha1.SHA1_DIGEST_SIZE;
import static tangible.cpp.common.HRESULT.E_FAIL;
import static tangible.cpp.common.HRESULT.E_INVALIDARG;
import static tangible.cpp.common.HRESULT.S_OK;

// ZipCrypto.h:11
public class NCrypto {

    // MyAes.h:14
    public static class CAesCbcCoder implements ICompressFilter, ICryptoProperties {

        public int _offset;
        public long _keySize;
        public boolean _keyIsSet;
        public boolean _encodeMode;
        public long[] _aes = new long[AES_NUM_IVMRK_WORDS + 3];
        private byte[] _iv = new byte[AES_BLOCK_SIZE];
        public Aes.AES_CODE_FUNC _codeFunc;

        public CAesCbcCoder(boolean encodeMode, int keySize) {
            _encodeMode = encodeMode;
            _keySize = keySize;
//            _offset = ((0 - (unsigned)(ptrdiff_t)_aes) & 0xF) / sizeof(UInt32);
            SetFunctions(0);
        }

        // MyAes.cpp:75
        public boolean SetFunctions(int algo) {
            _codeFunc = _encodeMode ? Aes::AesCbc_Encode : Aes::AesCbc_Decode;
            return true;
        }

        @Override
        public HRESULT Init() {
            AesCbc_Init(_aes, _iv);
            return _keyIsSet ? S_OK : E_FAIL;
        }

        // MyAes.cpp:221
        @Override
        public long Filter(byte[] data, int size) {
            if (!_keyIsSet)
                return 0;
            if (size == 0)
                return 0;
            if (size < AES_BLOCK_SIZE)
                return AES_BLOCK_SIZE;
            size >>= 4;
            _codeFunc.apply(_aes, _offset, data, 0, size);
            return size << 4;
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

        // MyAes.cpp:42
        @Override
        public HRESULT SetKey(byte[] data, int size) {
            if ((size & 0x7) != 0 || size < 16 || size > 32)
                return E_INVALIDARG;
            if (_keySize != 0 && size != _keySize)
                return E_INVALIDARG;

            if (_encodeMode)
                Aes.Aes_SetKey_Enc(_aes, _offset + 4, data, size);
            else
                Aes.Aes_SetKey_Dec(_aes, _offset + 4, data, size);

            _keyIsSet = true;
            return S_OK;
        }

        @Override
        public HRESULT SetInitVector(byte[] data, int size) {
            if (size != AES_BLOCK_SIZE)
                return E_INVALIDARG;

            System.arraycopy(data, 0, _iv, 0, size);
//            Init(); /*/// don't call virtual function here !!!
            return S_OK;
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
            public HRESULT CryptoSetPassword(byte[] data, int size) {
                return null;
            }

            @Override
            public long Filter(byte[] data, int size) {
                return 0;
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
            public HRESULT CryptoSetPassword(byte[] data, int size) {
                return null;
            }

            @Override
            public long Filter(byte[] data, int size) {
                return 0;
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
            public long Filter(byte[] data, int size) {
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

        // ZipStrong.cpp:39
        public static void DeriveKey(NSha1.CContext sha, byte[] key) {
            byte[] digest = new byte[NSha1.kDigestSize];
            sha.Final(digest, 0);
            byte[] temp = new byte[NSha1.kDigestSize * 2];
            DeriveKey2(digest, (byte)0x36, temp, 0);
            DeriveKey2(digest, (byte)0x5C, temp, NSha1.kDigestSize);

            for (int i = 0; i < 32; i++)
                key[i] = temp[i];

            int a = 0;
            a++;
        }

                    /*
  DeriveKey() function is similar to CryptDeriveKey() from Windows.
  New version of MSDN contains the following condition in CryptDeriveKey() description:
    "If the hash is not a member of the SHA-2 family and the required key is for either 3DES or AES".
  Now we support ZipStrong for AES only. And it uses SHA1.
  Our DeriveKey() code is equal to CryptDeriveKey() in Windows for such conditions: (SHA1 + AES).
  if (method != AES && method != 3DES), probably we need another code.
*/

        public static void DeriveKey2(byte[] digest, byte c, byte[] dest, int offs) {
            byte[] buf = new byte[64];
            Arrays.fill(buf, c);

            for (int i = 0; i < NSha1.kDigestSize; i++)
                buf[i] ^= digest[i];

            NSha1.CContext sha = new NSha1.CContext();
            sha.Init();
            sha.Update(buf, 64);
            sha.Final(dest, offs);
        }

        public static class CKeyInfo {

            public byte[] MasterKey = new byte[32];
            public int KeySize;

            // ZipStrong.cpp:49
            public void SetPassword(byte[] data, int size) {
                try {
                    NSha1.CContext sha = new NSha1.CContext();
                    sha.Init();
                    sha.Update(data, size);
                    DeriveKey(sha, MasterKey);
                } catch(Exception e) {
                    throw new RuntimeException(e);
                }
            }

        }

        // ZipStrong.h:32
        public static class CBaseCoder extends CAesCbcDecoder implements ICryptoSetPassword {

            public final CKeyInfo _key = new CKeyInfo();
            public byte[] /*CAlignedBuffer*/ _bufAligned;

            public CBaseCoder(int keySize) {
                super(keySize);
            }

            public HRESULT Init() {
                return super.Init();
            }

            @Override
            public HRESULT CryptoSetPassword(byte[] data, int size) {
                _key.SetPassword(data, size);
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

        // ZipStrong.h:46
        public static class CDecoder extends CBaseCoder {

            public static final int kPadSize = kAesPadAllign; // is equal to blockSize of cipher for rd

            public long _ivSize;
            public byte[] _iv = new byte[16];
            public long _remSize;

            public CDecoder(int keySize) {
                super(keySize);
            }

//            MY_UNKNOWN_IMP1(ICryptoSetPassword)

            // ZipStrong.cpp:68
            public HRESULT ReadHeader(ISequentialInStream inStream, long crc, long[] unpackSize) {
                return S_OK;
            }

            // ZipStrong.cpp:100
            public HRESULT Init_and_CheckPassword(boolean[] passwOK) {
                HRESULT __result__ = SetKey(_key.MasterKey, _key.KeySize);
                if (__result__ != S_OK)
                    return __result__;

                __result__ = SetInitVector(_iv, 16);
                if (__result__ != S_OK)
                    return __result__;

                __result__ = Init();
                if (__result__ != S_OK)
                    return __result__;

                int rdSize = 0;

                Filter(_bufAligned, rdSize);

                rdSize -= kPadSize;

//                for (int i = 0; i < kPadSize; i++)
//                    if (p[(size_t)rdSize + i] != kPadSize)
//                        return S_OK; // passwOK = false;

                return S_OK;
            }

            public long GetPadSize(long packSize32) {
                // Padding is to align to blockSize of cipher.
                // Change it, if is not AES
                return kAesPadAllign - (packSize32 & (kAesPadAllign - 1));
            }
        }
    }

    public static class NSha1 {

        public static final int kDigestSize = SHA1_DIGEST_SIZE;

        // Sha1Cls.h:17
        public abstract static class CContextBase {

            protected final MessageDigest sha;
//            public final CSha1 _s = new CSha1();

            public CContextBase() {
                try {
                    sha = MessageDigest.getInstance("SHA-1");
                } catch(Exception e) {
                    throw new RuntimeException(e);
                }
            }

            public void Init() {
//                Sha1_Init(_s);
                sha.reset();
            }

//            public void GetBlockDigest(const UInt32 *blockData, UInt32 *destDigest) {
//                Sha1_GetBlockDigest( & _s, blockData, destDigest);
//            }
        }

        // Sha1Cls.h:27
        public static class CContext extends CContextBase {

            public void Update(byte[] data, int size) {
                sha.update(data);
//                Sha1_Update(_s, data, size);
            }

//            public void UpdateRar(Byte *data, size_t size /* , bool rar350Mode */) {
//                Sha1_Update_Rar( & _s, data, size /* , rar350Mode ? 1 : 0 */);
//            }

            public void Final(byte[] digest, int offs) {
                try {
                    sha.digest(digest, offs, digest.length - offs);
//                Sha1_Final(_s, digest);
                } catch(Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }


}
