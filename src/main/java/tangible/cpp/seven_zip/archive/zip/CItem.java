package tangible.cpp.seven_zip.archive.zip;

public class CItem extends CLocalItem {

    public boolean FromLocal;
    public CExtraBlock CentralExtra;

    public boolean IsDir() {
        return false;
    }

    // ZipItem.h:312
    public CExtraBlock GetMainExtra() {
//        return *(FromCentral ? &CentralExtra : &LocalExtra);
        return null;
    }

}
