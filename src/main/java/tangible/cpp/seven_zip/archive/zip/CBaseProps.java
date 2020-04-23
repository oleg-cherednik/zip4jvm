package tangible.cpp.seven_zip.archive.zip;

// ZipCompressionMode.h:20
public class CBaseProps {

    boolean IsAesMode;
    byte AesKeyMode;

    public CBaseProps() {
        Init();
    }

    public void Init() {
        IsAesMode = false;
        AesKeyMode = 3;
    }

}
