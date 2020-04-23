package tangible.cpp.seven_zip.archive.zip;

public class NExtract {

    public enum NOperationResult {
        kOK,
        kUnsupportedMethod,
        kDataError,
        kCRCError,
        kUnavailable,
        kUnexpectedEnd,
        kDataAfterEnd,
        kIsNotArc,
        kHeadersError,
        kWrongPassword
    }

}
