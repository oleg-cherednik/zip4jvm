package tangible.cpp.seven_zip;

import tangible.cpp.common.HRESULT;
import tangible.cpp.common.IUnknown;

// IProgress.h:14
public interface IProgress extends IUnknown {

    HRESULT SetTotal(long total);

    HRESULT SetCompleted(long[] completeValue);
}
