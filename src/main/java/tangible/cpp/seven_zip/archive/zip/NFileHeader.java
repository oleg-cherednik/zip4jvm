package tangible.cpp.seven_zip.archive.zip;

// ZipHeader.h:35
public class NFileHeader {

    // ZipHeader.h:127
    public static class NFlags {

        public static final int kEncrypted = 1 << 0;
        public static final int kLzmaEOS = 1 << 1;
        public static final int kDescriptorUsedMask = 1 << 3;
        public static final int kStrongEncrypted = 1 << 6;
        public static final int kUtf8 = 1 << 11;
        public static final int kAltStream = 1 << 14;

        public static final int kImplodeDictionarySizeMask = 1 << 1;
        public static final int kImplodeLiteralsOnMask = 1 << 2;
    }

    // ZipHeader.h:39
    public static class NCompressionMethod {

        public static final int kMadeByProgramVersion = 63;

        public static final int kExtractVersion_Default = 10;
        public static final int kExtractVersion_Dir = 20;
        public static final int kExtractVersion_ZipCrypto = 20;
        public static final int kExtractVersion_Deflate = 20;
        public static final int kExtractVersion_Deflate64 = 21;
        public static final int kExtractVersion_Zip64 = 45;
        public static final int kExtractVersion_BZip2 = 46;
        public static final int kExtractVersion_Aes = 51;
        public static final int kExtractVersion_LZMA = 63;
        public static final int kExtractVersion_PPMd = 63;
        public static final int kExtractVersion_Xz = 20; // test it

        public enum EType {
            kStore,// = 0,
            kShrink,// = 1,
            kReduce1,// = 2,
            kReduce2,// = 3,
            kReduce3,// = 4,
            kReduce4,// = 5,
            kImplode,// = 6,
            kTokenize,// = 7,
            kDeflate,// = 8,
            kDeflate64,//= 9,
            kPKImploding,//= 10,

            kBZip2,// = 12,

            kLZMA,// = 14,

            kTerse,// = 18,
            kLz77,// = 19,

            kXz,// = 95,
            kJpeg,// = 96,
            kWavPack,// = 97,
            kPPMd,// = 98,
            kWzAES// = 99
        }
    }

}
