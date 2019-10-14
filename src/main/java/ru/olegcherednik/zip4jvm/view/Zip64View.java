package ru.olegcherednik.zip4jvm.view;

import lombok.Builder;
import ru.olegcherednik.zip4jvm.model.Zip64;

import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.Locale;
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
            out.format(Locale.US, "%sversion made by zip software (%02d):              %.1f\n",
                    prefix, dir.getVersionMadeBy(), (double)dir.getVersionMadeBy() / 10);
            out.format(Locale.US, "%sunzip software version needed to extract (%02d):  %.1f\n",
                    prefix, dir.getVersionToExtract(), (double)dir.getVersionToExtract() / 10);
            out.format("%spart number of this part (%04d):                %d\n", prefix, dir.getTotalDisks(), dir.getTotalDisks() + 1);
            out.format("%spart number of start of central dir (%04d):     %d\n", prefix, dir.getMainDisk(), dir.getMainDisk() + 1);
            out.format("%snumber of entries in central dir in this part:  %d\n", prefix, dir.getDiskEntries());
            out.format("%stotal number of entries in central dir:         %d\n", prefix, dir.getTotalEntries());
            out.format("%ssize of central dir:                            %2$d (0x%2$08X) bytes\n", prefix, dir.getCentralDirectorySize());
            out.format("%srelative offset of central dir:                 %2$d (0x%2$08X) bytes\n", prefix, dir.getCentralDirectoryOffs());

            /*
                    private long endCentralDirectorySize;
        // size:2 - version made by
        private int versionMadeBy;
        // size:2 - version needed to extractEntries
        private int versionNeededToExtract;
        // size:4 - number of this disk
        private long totalDisks;
        // size:4 - number of the disk with the start of the central directory
        private long mainDisk;
        // size:8 - total number of entries in the central directory on this disk
        private long diskEntries;
        // size:8 - total number of entries in the central directory
        private long totalEntries;
        // size:8 - size of the central directory
        private long centralDirectorySize;
        // size:8 - offs of CentralDirectory in startDiskNumber
        private long centralDirectoryOffs;
        // size:n-44 - extensible data sector
        private byte[] extensibleDataSector;
             */
        }
    }
}
