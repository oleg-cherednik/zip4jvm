package tangible.cpp.seven_zip.compress;

import tangible.cpp.common.GUID;
import tangible.cpp.common.HRESULT;
import tangible.cpp.seven_zip.ICompressCoder;
import tangible.cpp.seven_zip.ICompressProgressInfo;
import tangible.cpp.seven_zip.ISequentialInStream;
import tangible.cpp.seven_zip.archive.ISequentialOutStream;

// CopyCoder.h:10
public class NCompress {

    // CopyCoder.h:12
    public static class CCopyCoder implements ICompressCoder {

        @Override
        public HRESULT Code(ISequentialInStream inStream, ISequentialOutStream outStream, long inSize, long[] outSize,
                ICompressProgressInfo progress) {
            return null;
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

    // ShrinkDecoder.h:11
    public static class NShrink {

        public static class CDecoder implements ICompressCoder {

            @Override
            public HRESULT Code(ISequentialInStream inStream, ISequentialOutStream outStream, long inSize, long[] outSize,
                    ICompressProgressInfo progress) {
                return null;
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
    }

    // ImplodeDecoder.h:16
    public static class NImplode {

        // ImplodeDecoder.h:17
        public static class NDecoder {

            // ImplodeDecoder.h:35
            public static class CCoder implements ICompressCoder {

                @Override
                public HRESULT Code(ISequentialInStream inStream, ISequentialOutStream outStream, long inSize, long[] outSize,
                        ICompressProgressInfo progress) {
                    return null;
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
        }
    }

    // ZxDecoder.h:13
    public static class NXz {

        // XzDecoder.h:48
        public static class CComDecoder implements ICompressCoder {

            @Override
            public HRESULT Code(ISequentialInStream inStream, ISequentialOutStream outStream, long inSize, long[] outSize,
                    ICompressProgressInfo progress) {
                return null;
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

    }

    // PpmdZip.h:16
    public static class NPpmdZip {

        // PpmdZip.h:35
        public static class CDecoder implements ICompressCoder {

            public CDecoder(boolean fullFileMode) {

            }

            @Override
            public HRESULT Code(ISequentialInStream inStream, ISequentialOutStream outStream, long inSize, long[] outSize,
                    ICompressProgressInfo progress) {
                return null;
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
    }

    // LzmaDecoder.cpp:27
    public static class NLzma {

        public static class CDecoder {

            // LzmaDecoer.h:103
            public boolean CheckFinishStatus(boolean withEndMark) {
//                return _lzmaStatus == (withEndMark ?
//                                       LZMA_STATUS_FINISHED_WITH_MARK :
//                                       LZMA_STATUS_MAYBE_FINISHED_WITHOUT_MARK);
                return true;
            }
        }
    }
}
