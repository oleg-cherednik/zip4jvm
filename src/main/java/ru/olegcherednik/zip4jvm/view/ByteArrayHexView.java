package ru.olegcherednik.zip4jvm.view;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.io.PrintStream;
import java.util.Deque;
import java.util.LinkedList;

/**
 * @author Oleg Cherednik
 * @since 17.10.2019
 */
public final class ByteArrayHexView extends View {

    private final byte[] buf;

    public static Builder builder() {
        return new Builder();
    }

    private ByteArrayHexView(Builder builder) {
        super(builder.offs, builder.columnWidth);
        buf = builder.buf;
    }

    @Override
    public void print(PrintStream out) {
        if (ArrayUtils.isEmpty(buf))
            return;

        Deque<Integer> hexs = new LinkedList<>();

        for (int i = 0; i < buf.length; i++)
            hexs.add((int)buf[i]);

        final String empty = StringUtils.repeat(" ", offs);

        while (!hexs.isEmpty()) {
            out.print(empty);

            for (int i = 0; i < 16; i++) {
                if (hexs.isEmpty())
                    break;
                if (i > 0)
                    out.print(' ');
                out.format("%02X", hexs.remove().byteValue());
            }

            out.println();
        }
    }

    public static final class Builder {

        private byte[] buf;
        private int offs;
        private int columnWidth;

        public ByteArrayHexView build() {
            return new ByteArrayHexView(this);
        }

        @SuppressWarnings("MethodCanBeVariableArityMethod")
        public Builder buf(byte[] buf) {
            this.buf = ArrayUtils.clone(buf);
            return this;
        }

        public Builder offs(int offs) {
            this.offs = offs;
            return this;
        }

        public Builder columnWidth(int columnWidth) {
            this.columnWidth = columnWidth;
            return this;
        }
    }
}
