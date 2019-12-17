package ru.olegcherednik.zip4jvm.view;

import org.apache.commons.lang.StringUtils;

import java.io.PrintStream;
import java.nio.charset.Charset;

/**
 * @author Oleg Cherednik
 * @since 13.10.2019
 */
public final class StringHexView extends View {

    private final String str;
    private final Charset charset;

    public StringHexView(String str, Charset charset, int offs, int columnWidth) {
        super(offs, columnWidth);
        this.str = StringUtils.isEmpty(str) ? null : str;
        this.charset = charset;
    }

    @Override
    public boolean print(PrintStream out) {
        if (str == null)
            return false;

        printCharsetName(out);
        printLines(out);
        return true;
    }

    private void printCharsetName(PrintStream out) {
        printLine(out, "", charset.name());
    }

    private void printLines(PrintStream out) {
        int i = 0;

        while (i < str.length()) {
            StringBuilder one = new StringBuilder();
            StringBuilder two = new StringBuilder();

            while (i < str.length() && one.length() + 3 < columnWidth - offs) {
                char ch = str.charAt(i);
                byte[] data = String.valueOf(ch).getBytes(charset);

                if (one.length() + data.length * 3 > columnWidth - offs)
                    break;

                for (int j = 0; j < data.length; j++) {
                    if (one.length() > 0)
                        one.append(' ');
                    one.append(String.format("%02X", data[j]));
                }

                two.append(Character.isISOControl(ch) ? '.' : ch);
                i++;
            }

            printLine(out, one, two);
        }
    }

}
