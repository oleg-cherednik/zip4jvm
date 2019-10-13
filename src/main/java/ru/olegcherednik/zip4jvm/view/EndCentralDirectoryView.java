package ru.olegcherednik.zip4jvm.view;

import lombok.Builder;
import org.apache.commons.lang.StringUtils;
import ru.olegcherednik.zip4jvm.model.EndCentralDirectory;

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

    private final EndCentralDirectory endCentralDirectory;
    private final long offs;
    private final long size;
    private final Charset charset;
    private final String prefix;

    public void print(PrintStream out) {
        String str = String.format("End central directory record %s: %d bytes", ViewUtils.signature(EndCentralDirectory.SIGNATURE), size);
        out.println(str);

        IntStream.range(0, str.length()).forEach(i -> out.print('='));

        out.println();
        // TODO duplicate of ZipUtils.toString()
        out.format("%slocation of end-of-central-dir record:          %2$d (0x%2$08X) bytes\n", prefix, offs);
        out.format("%spart number of this part (%04d):                %d\n",
                prefix, endCentralDirectory.getTotalDisks(), endCentralDirectory.getTotalDisks() + 1);
        out.format("%spart number of start of central dir (%04d):     %d\n",
                prefix, endCentralDirectory.getMainDisk(), endCentralDirectory.getMainDisk() + 1);
        out.format("%snumber of entries in central dir in this part:  %d\n", prefix, endCentralDirectory.getDiskEntries());
        out.format("%stotal number of entries in central dir:         %d\n", prefix, endCentralDirectory.getTotalEntries());
        out.format("%ssize of central dir:                            %2$d (0x%2$08X) bytes\n",
                prefix, endCentralDirectory.getCentralDirectorySize());
        out.format("%srelative offset of central dir:                 %2$d (0x%2$08X) bytes\n",
                prefix, endCentralDirectory.getCentralDirectoryOffs());
        out.format("%szipfile comment length:                         %d bytes\n",
                prefix, Optional.ofNullable(endCentralDirectory.getComment()).orElse("").getBytes(charset).length);

        if (StringUtils.isNotEmpty(endCentralDirectory.getComment())) {
            StringHexView.builder()
                         .str(endCentralDirectory.getComment())
                         .charset(charset)
                         .prefix(prefix).build().print(out);
        }
    }


}

