package tangible.cpp.seven_zip;

import tangible.cpp.common.HRESULT;

public interface ICompressProgressInfo {

    HRESULT SetRatioInfo(int[] inSize, int[] outSize);
}
