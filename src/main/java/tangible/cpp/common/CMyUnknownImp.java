package tangible.cpp.common;

/**
 * @author Oleg Cherednik
 * @since 23.04.2020
 */
public abstract class CMyUnknownImp implements IUnknown {

    protected long __m_RefCount;

    public long AddRef() {
        return ++__m_RefCount;
    }

    public long Release() {
        return --__m_RefCount;
    }
}
