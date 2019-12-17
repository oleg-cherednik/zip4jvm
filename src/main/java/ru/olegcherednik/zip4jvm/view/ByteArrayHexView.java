package ru.olegcherednik.zip4jvm.view;

import org.apache.commons.lang.ArrayUtils;

import java.io.PrintStream;

/**
 * @author Oleg Cherednik
 * @since 17.10.2019
 */
public final class ByteArrayHexView extends View {

    private final byte[] data;

    public ByteArrayHexView(byte[] data, int offs, int columnWidth) {
        super(offs, columnWidth);
        this.data = ArrayUtils.isEmpty(data) ? ArrayUtils.EMPTY_BYTE_ARRAY : ArrayUtils.clone(data);
    }

    @Override
    public boolean print(PrintStream out) {
        int i = 0;
        int j = 0;

        while (i < data.length) {
            if (i > 0) {
                out.println();
                j = 0;
            }

            out.print(prefix);
            j += prefix.length();

            do {
                if (j > prefix.length()) {
                    out.print(' ');
                    j++;
                }

                out.format("%02X", data[i++]);
                j += 2;
            } while (i < data.length && j + 3 < columnWidth);
        }

        return data.length > 0;
    }

}
