package ru.olegcherednik.zip4jvm.view;

import lombok.Builder;

import java.io.PrintStream;
import java.nio.CharBuffer;
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
    @Builder.Default
    @SuppressWarnings("FieldMayBeStatic")
    private final String prefix = "";

    public void print(PrintStream out) {
        if (str == null)
            return;

        Deque<Integer> hexs = new LinkedList<>();
        Deque<Character> chars = new LinkedList<>();
        Deque<Integer> charsLength = new LinkedList<>();
        char[] arr = new char[1];

        for (int i = 0, j = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            arr[0] = ch;
            byte[] buf = charset.encode(CharBuffer.wrap(arr)).array();

            charsLength.add(buf.length);

            for (int k = 0; k < buf.length; k++) {
                hexs.add((int)buf[k]);

                if (k == buf.length - 1)
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
