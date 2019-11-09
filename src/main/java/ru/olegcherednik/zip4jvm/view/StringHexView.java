package ru.olegcherednik.zip4jvm.view;

import org.apache.commons.lang.StringUtils;
import ru.olegcherednik.zip4jvm.model.Charsets;

import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Optional;

/**
 * @author Oleg Cherednik
 * @since 13.10.2019
 */
public final class StringHexView extends View {

    private final String str;
    private final Charset charset;

    public static Builder builder() {
        return new Builder();
    }

    private StringHexView(Builder builder) {
        super(builder.offs, builder.columnWidth);
        str = builder.str;
        charset = builder.charset;
    }

    @Override
    public void print(PrintStream out) {
        if (str == null)
            return;

        printLine(out, "", charset.name());

        Deque<Integer> hexs = new LinkedList<>();
        Deque<Character> chars = new LinkedList<>();
        Deque<Integer> charsLength = new LinkedList<>();

        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            byte[] buf = String.valueOf(ch).getBytes(charset);

            charsLength.add(buf.length);

            for (int j = 0; j < buf.length; j++) {
                hexs.add((int)buf[j]);

                if (j == buf.length - 1)
                    chars.add(Character.isISOControl(ch) ? '.' : ch);
            }
        }

        while (!hexs.isEmpty()) {
            out.print(prefix);

            for (int i = 0; i < 16; i++) {
                if (hexs.isEmpty())
                    out.print("   ");
                else
                    out.format("%02X ", hexs.remove().byteValue());
            }

            for (int i = 0; i < 16; i += Optional.ofNullable(charsLength.poll()).orElse(1)) {
                if (chars.isEmpty())
                    out.print("   ");
                else
                    out.format("%s", chars.remove());
            }

            out.println();
        }
    }

    public static final class Builder {

        private String str;
        private Charset charset = Charsets.IBM437;
        private int offs;
        private int columnWidth;

        public StringHexView build() {
            return new StringHexView(this);
        }

        public Builder str(String str) {
            this.str = StringUtils.isEmpty(str) ? null : str;
            return this;
        }

        public Builder charset(Charset charset) {
            this.charset = Optional.ofNullable(charset).orElse(Charsets.IBM437);
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
