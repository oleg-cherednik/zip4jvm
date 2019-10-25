package ru.olegcherednik.zip4jvm.io.readers.os;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.model.os.InfoZipUnixExtraField;
import ru.olegcherednik.zip4jvm.utils.function.Reader;
import ru.olegcherednik.zip4jvm.utils.time.UnixTimestampConverter;

import java.io.IOException;

import static ru.olegcherednik.zip4jvm.model.ExtraField.NO_DATA;

/**
 * @author Oleg Cherednik
 * @since 25.10.2019
 */
@RequiredArgsConstructor
public final class InfoZipUnitExtraFieldReader implements Reader<InfoZipUnixExtraField> {

    private final int size;

    @Override
    public InfoZipUnixExtraField read(DataInput in) throws IOException {
        long lastAccessTime = UnixTimestampConverter.unixToJavaTime(in.readDword());
        long lastModificationTime = UnixTimestampConverter.unixToJavaTime(in.readDword());
        int uid = size >= 10 ? in.readWord() : NO_DATA;
        int gid = size >= 12 ? in.readWord() : NO_DATA;

        return InfoZipUnixExtraField.builder()
                                    .dataSize(size)
                                    .lastAccessTime(lastAccessTime)
                                    .lastModificationTime(lastModificationTime)
                                    .uid(uid)
                                    .gid(gid).build();
    }

}
