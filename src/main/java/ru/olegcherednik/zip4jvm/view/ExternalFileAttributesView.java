package ru.olegcherednik.zip4jvm.view;

import lombok.Builder;
import ru.olegcherednik.zip4jvm.model.ExternalFileAttributes;
import ru.olegcherednik.zip4jvm.utils.BitUtils;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT0;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT1;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT2;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT3;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT4;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT5;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT6;

/**
 * @author Oleg Cherednik
 * @since 15.10.2019
 */
@Builder
public class ExternalFileAttributesView {

    private final ExternalFileAttributes externalFileAttributes;
    private final String prefix;

    public void print(PrintStream out) {
        byte[] data = externalFileAttributes.get();
        int val = data[3] << 24 | data[2] << 16 | data[1] << 8 | data[0];

        out.format("%sexternal file attributes:                       0x%08X\n", prefix, val);
        out.format("%s  MS-DOS file attributes (0x%02X)                 %s\n", prefix, val & 0xFF, getMsDosAttributes(data[0]));
        out.format("%s  non-MSDOS file attributes (0x%06X):         --\n", prefix, val >> 8);
    }

    // TODO move to ExternalFileAttributes implementation
    private static String getMsDosAttributes(byte data) {
        List<String> attributes = new ArrayList<>(4);

        if (BitUtils.isBitSet(data, BIT0))
            attributes.add("rdo");
        if (BitUtils.isBitSet(data, BIT1))
            attributes.add("hid");
        if (BitUtils.isBitSet(data, BIT2))
            attributes.add("sys");
        if (BitUtils.isBitSet(data, BIT3))
            attributes.add("arc");
        if (BitUtils.isBitSet(data, BIT4))
            attributes.add("lab");
        if (BitUtils.isBitSet(data, BIT5))
            attributes.add("dir");
        if (BitUtils.isBitSet(data, BIT6))
            attributes.add("arc");

        if (attributes.isEmpty())
            return "none";
        if (attributes.size() == 1 && "rdo".equals(attributes.get(0)))
            return "read-only";
        return String.join(" ", attributes);
    }
}
