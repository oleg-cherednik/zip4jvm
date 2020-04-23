package tangible.cpp.seven_zip.archive.zip;

public class CLocalItem {

    public int Flags;
    public NFileHeader.NCompressionMethod.EType Method;
    public long Size;
    public long PackSize;
    public long Time;
    public long Crc;

    public boolean IsEncrypted() {
        return (Flags & NFileHeader.NFlags.kEncrypted) != 0;
    }

    public boolean IsStrongEncrypted() {
        return IsEncrypted() && (Flags & NFileHeader.NFlags.kStrongEncrypted) != 0;
    }

    public boolean HasDescriptor() {
        return (Flags & NFileHeader.NFlags.kDescriptorUsedMask) != 0;
    }

}
