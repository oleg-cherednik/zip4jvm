package ru.olegcherednik.zip4jvm.view;

import lombok.Builder;
import org.apache.commons.lang.StringUtils;
import ru.olegcherednik.zip4jvm.model.EndCentralDirectory;
import ru.olegcherednik.zip4jvm.model.block.Block;

import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * @author Oleg Cherednik
 * @since 13.10.2019
 */
@Builder
public class EndCentralDirectoryView {

    private final EndCentralDirectory dir;
    private final Block block;
    private final Charset charset;
    private final String prefix;

    public void print(PrintStream out) {
        String str = String.format("End central directory record %s: %d bytes", ViewUtils.signature(EndCentralDirectory.SIGNATURE), block.getSize());
        out.println(str);

        IntStream.range(0, str.length()).forEach(i -> out.print('='));

        out.println();
        // TODO duplicate of ZipUtils.toString()
        out.format("%slocation of end-of-central-dir record:          %2$d (0x%2$08X) bytes\n", prefix, block.getOffs());
        out.format("%spart number of this part (%04X):                %d\n", prefix, dir.getTotalDisks(), dir.getTotalDisks() + 1);
        out.format("%spart number of start of central dir (%04X):     %d\n", prefix, dir.getMainDisk(), dir.getMainDisk() + 1);
        out.format("%snumber of entries in central dir in this part:  %d\n", prefix, dir.getDiskEntries());
        out.format("%stotal number of entries in central dir:         %d\n", prefix, dir.getTotalEntries());
        out.format("%ssize of central dir:                            %2$d (0x%2$08X) bytes\n", prefix, dir.getCentralDirectorySize());
        out.format("%srelative offset of central dir:                 %2$d (0x%2$08X) bytes\n", prefix, dir.getCentralDirectoryOffs());
        out.format("%szipfile comment length:                         %d bytes\n",
                prefix, Optional.ofNullable(dir.getComment()).orElse("").getBytes(charset).length);

        if (StringUtils.isNotEmpty(dir.getComment())) {
            StringHexView.builder()
                         .str(dir.getComment())
                         .charset(charset)
                         .prefix(prefix).build().print(out);
        }
    }
}

