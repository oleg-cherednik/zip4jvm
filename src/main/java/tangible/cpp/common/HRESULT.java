package tangible.cpp.common;

public enum HRESULT {
    S_OK,
    S_FALSE,
    E_NOTIMP,       //unsupported properties
    E_NOINTERFACE,
    E_ABORT,
    E_FAIL,
    STG_E_INVALIDFUNCTION,
    E_INVALIDARG,   // incorrect (or unsupported) properties
    E_OUTOFMEMORY;  // memory allocation error

}
