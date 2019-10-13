package ru.olegcherednik.zip4jvm.view;

import lombok.Builder;
import ru.olegcherednik.zip4jvm.model.Zip64;

import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.stream.IntStream;

/**
 * @author Oleg Cherednik
 * @since 14.10.2019
 */
public class Zip64View {

    @Builder
    public static class EndCentralDirectoryLocator {

        private final Zip64.EndCentralDirectoryLocator locator;
        private final long offs;
        private final long size;
        private final Charset charset;
        private final String prefix;

        public void print(PrintStream out) {
            if (locator == null)
                return;

            String str = String.format("New End of Central directory locator %s: %d bytes",
                    ViewUtils.signature(Zip64.EndCentralDirectoryLocator.SIGNATURE), size);
            out.println(str);

            IntStream.range(0, str.length()).forEach(i -> out.print('='));

            out.println();
            out.format("%slocation of new-end-of-central-dir-locator:     %2$d (0x%2$08X) bytes\n", prefix, offs);
            out.format("%spart number of new-end-of-central-dir (%04d):   %d\n",
                    prefix, locator.getMainDisk(), locator.getMainDisk() + 1);
            out.format("%srelative offset of new-end-of-central-dir:      %2$d (0x%2$08X) bytes\n", prefix, locator.getOffs());
            out.format("%stotal number of parts in archive:               %d\n", prefix, locator.getTotalDisks());
        }
    }
}
