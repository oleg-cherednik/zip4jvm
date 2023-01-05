package ru.olegcherednik.zip4jvm.model.extrafield;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.readers.ExtraFieldRecordReader;

/**
 * It was faced in <tt>apk</tt> file. This is not a PKWARE standard extra field.
 * Store it as simple byte array. It should not be greater than
 * {@link  ExtraFieldRecordReader#getHeaderSize(DataInput)}}.
 *
 * @author Oleg Cherednik
 * @since 05.01.2023
 */
@Getter
@RequiredArgsConstructor
public class AlignmentExtraField implements IExtraField {

    private final byte[] data;

    @Override
    public int getSize() {
        return data.length;
    }

}
