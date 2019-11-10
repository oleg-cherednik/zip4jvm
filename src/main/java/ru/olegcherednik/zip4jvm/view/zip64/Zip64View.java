package ru.olegcherednik.zip4jvm.view.zip64;

import lombok.Builder;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.block.Diagnostic;
import ru.olegcherednik.zip4jvm.view.VersionView;
import ru.olegcherednik.zip4jvm.view.ViewUtils;

import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.stream.IntStream;

/**
 * @author Oleg Cherednik
 * @since 14.10.2019
 */
@Builder
public class Zip64View {

    private final Zip64 zip64;
    private final Diagnostic.Zip64 diagZip64;
    private final Charset charset;
    private final String prefix;

    public void print(PrintStream out) {
        printEndCentralDirectorLocator(out);
        out.println();

        EndCentralDirectory.builder()
                           .dir(zip64.getEndCentralDirectory())
                           .block(diagZip64.getEndCentralDirectory())
                           .charset(charset)
                           .prefix(prefix).build().print(out);
    }

    private void printEndCentralDirectorLocator(PrintStream out) {
        EndCentralDirectoryLocatorView.builder()
                                      .locator(zip64.getEndCentralDirectoryLocator())
                                      .block(diagZip64.getEndCentralDirectoryLocator())
                                      .offs(prefix.length())
                                      .columnWidth(52).build().print(out);
    }

    @Builder
    public static class EndCentralDirectory {

        private final Zip64.EndCentralDirectory dir;
        private final Block block;
        private final Charset charset;
        private final String prefix;

        public void print(PrintStream out) {
            if (dir == null)
                return;

            String str = String.format("New End of Central directory %s: %d bytes",
                    ViewUtils.signature(Zip64.EndCentralDirectory.SIGNATURE), block.getSize());
            out.println(str);

            IntStream.range(0, str.length()).forEach(i -> out.print('='));

            out.println();
            out.format("%slocation of new-end-of-central-dir:             %2$d (0x%2$08X) bytes\n", prefix, block.getOffs());
            out.format("%snumber of bytes in rest of record:              %d bytes\n", prefix, dir.getEndCentralDirectorySize());

            VersionView.builder()
                       .versionMadeBy(dir.getVersionMadeBy())
                       .versionToExtract(dir.getVersionToExtract())
                       .offs(prefix.length())
                       .columnWidth(52).build().print(out);

            out.format("%spart number of this part (%04d):                %d\n", prefix, dir.getTotalDisks(), dir.getTotalDisks() + 1);
            out.format("%spart number of start of central dir (%04d):     %d\n", prefix, dir.getMainDisk(), dir.getMainDisk() + 1);
            out.format("%snumber of entries in central dir in this part:  %d\n", prefix, dir.getDiskEntries());
            out.format("%stotal number of entries in central dir:         %d\n", prefix, dir.getTotalEntries());
            out.format("%ssize of central dir:                            %2$d (0x%2$08X) bytes\n", prefix, dir.getCentralDirectorySize());
            out.format("%srelative offset of central dir:                 %2$d (0x%2$08X) bytes\n", prefix, dir.getCentralDirectoryOffs());
        }
    }
}
