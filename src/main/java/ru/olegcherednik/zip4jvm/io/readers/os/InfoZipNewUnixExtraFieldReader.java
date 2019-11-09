package ru.olegcherednik.zip4jvm.io.readers.os;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.model.os.InfoZipNewUnixExtraField;
import ru.olegcherednik.zip4jvm.utils.function.Reader;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 25.10.2019
 */
@RequiredArgsConstructor
public final class InfoZipNewUnixExtraFieldReader implements Reader<InfoZipNewUnixExtraField> {

    private final int size;

    @Override
    public InfoZipNewUnixExtraField read(DataInput in) throws IOException {
        int version = in.readByte();

        InfoZipNewUnixExtraField.Payload payload = version == 1 ? readVersionOnePayload(in) : readVersionUnknown(version, in);

        return InfoZipNewUnixExtraField.builder()
                                       .dataSize(size)
                                       .payload(payload).build();
    }

    private static InfoZipNewUnixExtraField.VersionOnePayload readVersionOnePayload(DataInput in) throws IOException {
        String uid = in.readNumber(in.readByte(), 16);
        String gid = in.readNumber(in.readByte(), 16);

        return InfoZipNewUnixExtraField.VersionOnePayload.builder()
                                                         .uid(uid)
                                                         .gid(gid).build();
    }

    private InfoZipNewUnixExtraField.VersionUnknownPayload readVersionUnknown(int version, DataInput in) throws IOException {
        byte[] data = in.readBytes(size - in.byteSize());
        return InfoZipNewUnixExtraField.VersionUnknownPayload.builder()
                                                             .version(version)
                                                             .data(data).build();
    }
}
