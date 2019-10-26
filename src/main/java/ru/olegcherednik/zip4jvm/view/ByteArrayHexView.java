package ru.olegcherednik.zip4jvm.view;

import lombok.Builder;
import org.apache.commons.lang.ArrayUtils;

import java.io.PrintStream;
import java.util.Deque;
import java.util.LinkedList;

/**
 * @author Oleg Cherednik
 * @since 17.10.2019
 */
@Builder
public final class ByteArrayHexView {

    private final byte[] buf;
    private final String prefix;

    public void print(PrintStream out) {
        if (ArrayUtils.isEmpty(buf))
            return;

        Deque<Integer> hexs = new LinkedList<>();

        for (int i = 0; i < buf.length; i++)
            hexs.add((int)buf[i]);

        while (!hexs.isEmpty()) {
            out.print(prefix);

            for (int i = 0; i < 16; i++) {
                if (hexs.isEmpty())
                    out.print("   ");
                else
                    out.format("%02X ", hexs.remove().byteValue());
            }

            out.println();
        }
    }
}
