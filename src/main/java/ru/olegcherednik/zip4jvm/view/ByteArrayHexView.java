package ru.olegcherednik.zip4jvm.view;

import org.apache.commons.lang3.ArrayUtils;

import java.io.PrintStream;

/**
 * @author Oleg Cherednik
 * @since 17.10.2019
 */
public final class ByteArrayHexView extends BaseView {

    private final byte[] data;

    public ByteArrayHexView(byte[] data, int offs, int columnWidth) {
        super(offs, columnWidth);
        this.data = ArrayUtils.isEmpty(data) ? ArrayUtils.EMPTY_BYTE_ARRAY : ArrayUtils.clone(data);
    }

    @Override
    public boolean print(PrintStream out) {
        int i = 0;

        while (i < data.length) {
            StringBuilder one = new StringBuilder();

            do {
                if (one.length() > 0)
                    one.append(' ');
                one.append(String.format("%02X", data[i++]));
            } while (i < data.length && one.length() + 3 < columnWidth - offs);

            printLine(out, one);
        }

        return data.length > 0;
    }

}
