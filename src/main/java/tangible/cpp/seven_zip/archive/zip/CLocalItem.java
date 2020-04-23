package tangible.cpp.seven_zip.archive.zip;

public class CLocalItem {

    public int Flags;
    public NFileHeader.NCompressionMethod.EType Method;
    public long[] Size = new long[1];
    public long PackSize;
    public long Time;
    public long Crc;

    public CExtraBlock LocalExtra;

    public boolean IsEncrypted() {
        return (Flags & NFileHeader.NFlags.kEncrypted) != 0;
    }

    public boolean IsStrongEncrypted() {
        return IsEncrypted() && (Flags & NFileHeader.NFlags.kStrongEncrypted) != 0;
    }

    public boolean HasDescriptor() {
        return (Flags & NFileHeader.NFlags.kDescriptorUsedMask) != 0;
    }

    public boolean IsLzmaEOS() {
        return (Flags & NFileHeader.NFlags.kLzmaEOS) != 0;
    }

}
