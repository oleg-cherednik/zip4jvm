package ru.olegcherednik.zip4jvm.view;

import lombok.Builder;
import ru.olegcherednik.zip4jvm.model.LocalFileHeader;
import ru.olegcherednik.zip4jvm.model.block.BlockZipEntryModel;

import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.stream.IntStream;

/**
 * @author Oleg Cherednik
 * @since 20.10.2019
 */
@Builder
public class ZipEntryView {

    private final BlockZipEntryModel blockZipEntryModel;
    private final Charset charset;
    private final String prefix;

    public void print(PrintStream out) {
        if (blockZipEntryModel == null || blockZipEntryModel.getLocalFileHeaders() == null || blockZipEntryModel.getLocalFileHeaders().isEmpty())
            return;

        String str = "Zip entry:";
        out.println(str);

        IntStream.range(0, str.length()).forEach(i -> out.print('='));

        out.println();
        out.format("%stotal number of entries:                        %d\n", prefix, blockZipEntryModel.getLocalFileHeaders().size());

        out.println();
        printZipEntries(out);


        int a = 0;
        a++;
    }

    private void printZipEntries(PrintStream out) {
        int pos = 0;

        System.err.println("---------------------------");

        for (LocalFileHeader localFileHeader : blockZipEntryModel.getLocalFileHeaders().values()) {
            if (pos != 0)
                out.println();

            LocalFileHeaderView.builder()
                               .localFileHeader(localFileHeader)
                               .diagLocalFileHeader(blockZipEntryModel.getZipEntryBlock().getLocalFileHeader(localFileHeader.getFileName()))
                               .pos(pos)
                               .charset(charset)
                               .prefix(prefix).build().print(out);

            out.println();
            EncryptionHeaderView.builder()
                                .encryptionHeader(blockZipEntryModel.getZipEntryBlock().getEncryptionHeader(localFileHeader.getFileName()))
                                .pos(pos)
                                .charset(charset)
                                .prefix(prefix).build().print(out);

            pos++;
        }
    }
}
