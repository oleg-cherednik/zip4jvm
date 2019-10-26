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
public final class InfoZipNewUnitExtraFieldReader implements Reader<InfoZipNewUnixExtraField> {

    private final int size;

    @Override
    public InfoZipNewUnixExtraField read(DataInput in) throws IOException {
        int version = in.readByte();
        return version == 1 ? readVersionOne(in) : readVersionUnknown(version, in);
    }

    private InfoZipNewUnixExtraField readVersionOne(DataInput in) throws IOException {
        String uid = in.readNumber(in.readByte(), 16);
        String gid = in.readNumber(in.readByte(), 16);

        return InfoZipNewUnixExtraField.builder()
                                       .dataSize(size)
                                       .version(1)
                                       .payload(InfoZipNewUnixExtraField.VersionOnePayload.builder()
                                                                                          .uid(uid)
                                                                                          .gid(gid).build())
                                       .build();
    }

    private InfoZipNewUnixExtraField readVersionUnknown(int version, DataInput in) throws IOException {
        byte[] data = in.readBytes(size - in.byteSize());

        return InfoZipNewUnixExtraField.builder()
                                       .dataSize(size)
                                       .version(version)
                                       .payload(InfoZipNewUnixExtraField.VersionUnknownPayload.builder().data(data).build())
                                       .build();
    }
}
