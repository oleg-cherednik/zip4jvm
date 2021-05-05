package ru.olegcherednik.zip4jvm.view;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import ru.olegcherednik.zip4jvm.model.block.Block;

import java.io.PrintStream;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.IntStream;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireZeroOrPositive;

/**
 * @author Oleg Cherednik
 * @since 05.11.2019
 */
@Getter
public abstract class BaseView implements View {

    protected final int offs;
    protected final int columnWidth;
    protected final long totalDisks;
    protected final String format;
    protected final String prefix;

    protected BaseView(int offs, int columnWidth) {
        this(offs, columnWidth, -1);
    }

    protected BaseView(int offs, int columnWidth, long totalDisks) {
        this.offs = offs;
        this.columnWidth = columnWidth;
        this.totalDisks = totalDisks;
        format = "%-" + columnWidth + "s%s";
        prefix = StringUtils.repeat(" ", offs);
    }

    public final void printLine(PrintStream out, Object one, Object two) {
        one = Optional.ofNullable(one).orElse("");
        two = Optional.ofNullable(two).orElse("");

        if (offs > 0)
            one = prefix + one;

        out.format(Locale.US, format, one, two);
        out.println();
    }

    public void printLine(PrintStream out, Object one) {
        one = Optional.ofNullable(one).orElse("");

        if (offs > 0)
            one = prefix + one;

        out.println(one);
    }

    public void printTitle(PrintStream out, String str) {
        out.println(str);
        IntStream.range(0, str.length()).forEach(i -> out.print('='));
        out.println();
    }

    public void printTitle(PrintStream out, int signature, String title) {
        printTitle(out, String.format("(%s) %s", signature(signature), title));
    }

    public void printTitle(PrintStream out, int signature, String title, Block block) {
        printTitle(out, String.format("(%s) %s", signature(signature), title));
        printLocationAndSize(out, block);
    }

    public void printSubTitle(PrintStream out, int signature, long pos, String title, Block block) {
        String str = String.format("#%d (%s) %s", pos + 1, signature(signature), title);
        out.println(str);
        IntStream.range(0, str.length()).forEach(i -> out.print('-'));
        out.println();
        printLocationAndSize(out, block);
    }

    public void printSubTitle(PrintStream out, long pos, String title, Block block) {
        String str = String.format("#%d %s", pos + 1, title);
        out.println(str);
        IntStream.range(0, str.length()).forEach(i -> out.print('-'));
        out.println();
        printLocationAndSize(out, block);
    }

    public void printValueWithLocation(PrintStream out, String valueName, Block block) {
        printLine(out, valueName, String.format("%1$d (0x%1$08X) bytes", block.getRelativeOffs()));

        requireZeroOrPositive(totalDisks, "BaseView.totalDisks");

        if (totalDisks > 0)
            printLine(out, String.format("  - disk (%04X):", block.getDiskNo()), block.getFileName());

        printLine(out, "  - size:", String.format("%s bytes", block.getSize()));
    }

    public void printValueWithLocation(PrintStream out, String valueName, Block block, int total) {
        printLine(out, valueName, String.format("%1$d (0x%1$08X) bytes", block.getRelativeOffs()));

        requireZeroOrPositive(totalDisks, "BaseView.totalDisks");

        if (totalDisks > 0)
            printLine(out, String.format("  - disk (%04X):", block.getDiskNo()), block.getFileName());

        printLine(out, "  - size:", String.format("%d bytes (%d record%s)", block.getSize(), total, total == 1 ? "" : "s"));
    }

    @Deprecated
    public void printSubTitle(PrintStream out, long pos, String title) {
        String str = String.format("#%d %s", pos + 1, title);
        out.println(str);
        IntStream.range(0, str.length()).forEach(i -> out.print('-'));
        out.println();
    }

    private void printLocationAndSize(PrintStream out, Block block) {
        requireZeroOrPositive(totalDisks, "BaseView.totalDisks");

        if (totalDisks > 0)
            printLine(out, String.format("- disk (%04X):", block.getDiskNo()), block.getFileName());

        printLine(out, "- location:", String.format("%1$d (0x%1$08X) bytes", block.getRelativeOffs()));
        printLine(out, "- size:", String.format("%s bytes", block.getSize()));
    }

    public static String signature(int signature) {
        StringBuilder buf = new StringBuilder();

        for (int i = 0; i < 4; i++) {
            byte code = (byte)signature;

            if (Character.isAlphabetic((char)code) || Character.isDigit((char)code))
                buf.append((char)code);
            else
                buf.append(code < 10 ? "0" + code : code);

            signature >>= 8;
        }

        return buf.toString();
    }

}
