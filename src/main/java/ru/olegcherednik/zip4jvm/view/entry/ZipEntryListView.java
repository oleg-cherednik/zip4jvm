package ru.olegcherednik.zip4jvm.view.entry;

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
public class ZipEntryListView {

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
    }

    private void printZipEntries(PrintStream out) {
        int pos = 0;

        for (LocalFileHeader localFileHeader : blockZipEntryModel.getLocalFileHeaders().values()) {
            if (pos != 0)
                out.println();

            ZipEntryView.builder()
                        .pos(pos)
                        .localFileHeader(localFileHeader)
                        .diagLocalFileHeader(blockZipEntryModel.getZipEntryBlock().getLocalFileHeader(localFileHeader.getFileName()))
                        .encryptionHeader(blockZipEntryModel.getZipEntryBlock().getEncryptionHeader(localFileHeader.getFileName()))
                        .dataDescriptor(blockZipEntryModel.getDataDescriptors().get(localFileHeader.getFileName()))
                        .blockDataDescriptor(blockZipEntryModel.getZipEntryBlock().getDataDescriptor(localFileHeader.getFileName()))
                        .charset(charset)
                        .prefix(prefix)
                        .build().print(out);

            pos++;
        }
    }
}
