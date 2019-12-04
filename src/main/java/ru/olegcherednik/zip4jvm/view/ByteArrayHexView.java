package ru.olegcherednik.zip4jvm.view;

import org.apache.commons.lang.ArrayUtils;

import java.io.PrintStream;
import java.util.Deque;
import java.util.LinkedList;

/**
 * @author Oleg Cherednik
 * @since 17.10.2019
 */
public final class ByteArrayHexView extends View {

    private final byte[] data;

    public static Builder builder() {
        return new Builder();
    }

    private ByteArrayHexView(Builder builder) {
        super(builder.offs, builder.columnWidth);
        data = builder.data;
    }

    @Override
    public boolean print(PrintStream out) {
        if (ArrayUtils.isEmpty(data))
            return false;

        Deque<Integer> hexs = new LinkedList<>();

        for (int i = 0; i < data.length; i++)
            hexs.add((int)data[i]);

        while (!hexs.isEmpty()) {
            out.print(prefix);

            for (int i = 0; i < 16; i++) {
                if (hexs.isEmpty())
                    break;
                if (i > 0)
                    out.print(' ');
                out.format("%02X", hexs.remove().byteValue());
            }

            out.println();
        }

        return true;
    }

    public static final class Builder {

        private byte[] data;
        private int offs;
        private int columnWidth;

        public ByteArrayHexView build() {
            return new ByteArrayHexView(this);
        }

        @SuppressWarnings("MethodCanBeVariableArityMethod")
        public Builder data(byte[] data) {
            this.data = ArrayUtils.isEmpty(data) ? ArrayUtils.EMPTY_BYTE_ARRAY : ArrayUtils.clone(data);
            return this;
        }

        public Builder position(int offs, int columnWidth) {
            this.offs = offs;
            this.columnWidth = columnWidth;
            return this;
        }

    }
}
