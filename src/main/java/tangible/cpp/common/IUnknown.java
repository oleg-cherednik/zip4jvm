package tangible.cpp.common;

public interface IUnknown {

    HRESULT QueryInterface(GUID iid, Object[] outObject);

    long AddRef();

    long Release();

}
