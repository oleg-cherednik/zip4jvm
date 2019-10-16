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
            out.format("%spart number of new-end-of-central-dir (%04X):   %d\n",
                    prefix, locator.getMainDisk(), locator.getMainDisk() + 1);
            out.format("%srelative offset of new-end-of-central-dir:      %2$d (0x%2$08X) bytes\n", prefix, locator.getOffs());
            out.format("%stotal number of parts in archive:               %d\n", prefix, locator.getTotalDisks());
        }
    }

    @Builder
    public static class EndCentralDirectory {

        private final Zip64.EndCentralDirectory dir;
        private final long offs;
        private final long size;
        private final Charset charset;
        private final String prefix;

        public void print(PrintStream out) {
            if (dir == null)
                return;

            String str = String.format("New End of Central directory %s: %d bytes", ViewUtils.signature(Zip64.EndCentralDirectory.SIGNATURE), size);
            out.println(str);

            IntStream.range(0, str.length()).forEach(i -> out.print('='));

            out.println();
            out.format("%slocation of new-end-of-central-dir:             %2$d (0x%2$08X) bytes\n", prefix, offs);
            out.format("%snumber of bytes in rest of record:              %d bytes\n", prefix, dir.getEndCentralDirectorySize());

            VersionView.builder()
                       .versionMadeBy(dir.getVersionMadeBy())
                       .versionToExtract(dir.getVersionToExtract())
                       .prefix(prefix).build().print(out);

            out.format("%spart number of this part (%04d):                %d\n", prefix, dir.getTotalDisks(), dir.getTotalDisks() + 1);
            out.format("%spart number of start of central dir (%04d):     %d\n", prefix, dir.getMainDisk(), dir.getMainDisk() + 1);
            out.format("%snumber of entries in central dir in this part:  %d\n", prefix, dir.getDiskEntries());
            out.format("%stotal number of entries in central dir:         %d\n", prefix, dir.getTotalEntries());
            out.format("%ssize of central dir:                            %2$d (0x%2$08X) bytes\n", prefix, dir.getCentralDirectorySize());
            out.format("%srelative offset of central dir:                 %2$d (0x%2$08X) bytes\n", prefix, dir.getCentralDirectoryOffs());
        }
    }
}
