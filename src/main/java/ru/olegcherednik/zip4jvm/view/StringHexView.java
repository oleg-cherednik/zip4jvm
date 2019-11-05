package ru.olegcherednik.zip4jvm.view;

import lombok.Builder;

import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Optional;

/**
 * @author Oleg Cherednik
 * @since 13.10.2019
 */
@Builder
public final class StringHexView {

    private final String str;
    private final Charset charset;
    private final String prefix;

    public void print(PrintStream out) {
        if (str == null)
            return;

        out.format("%s                                                %s\n", prefix, charset.name());

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
}
