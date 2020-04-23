package tangible.cpp.seven_zip.archive.zip;

// ZipIn.h:38
public class CInArchiveInfo {
    public long Base; /* Base offset of start of archive in stream.
                 Offsets in headers must be calculated from that Base.
                 Base is equal to MarkerPos for normal ZIPs.
                 Base can point to PE stub for some ZIP SFXs.
                 if CentralDir was read,
                   Base can be negative, if start of data is not available,
                 if CentralDirs was not read,
                   Base = ArcInfo.MarkerPos; */
}
