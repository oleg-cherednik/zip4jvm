package tangible.cpp.seven_zip.archive.zip;

// ZipItem.h:141
public class CExtraBlock {

    public CExtraSubBlock[] SubBlocks;
    boolean Error;
    boolean MinorError;
    boolean IsZip64;
    boolean IsZip64_Error;


    boolean GetStrongCrypto(CStrongCryptoExtra e) {
        for (int i = 0; i < SubBlocks.length; i++)
            if (e.ParseFromSubBlock(SubBlocks[i]))
                return true;
        return false;
    }

    public boolean GetWzAes(CWzAesExtra e) {
        for (int i = 0; i < SubBlocks.length; i++)
            if (e.ParseFromSubBlock(SubBlocks[i]))
            return true;
        return false;
    }
}
