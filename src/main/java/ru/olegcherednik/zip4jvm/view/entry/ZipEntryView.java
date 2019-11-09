package ru.olegcherednik.zip4jvm.view.entry;

import lombok.Builder;
import ru.olegcherednik.zip4jvm.model.DataDescriptor;
import ru.olegcherednik.zip4jvm.model.LocalFileHeader;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.block.Diagnostic;
import ru.olegcherednik.zip4jvm.view.DataDescriptorView;
import ru.olegcherednik.zip4jvm.view.EncryptionHeaderView;
import ru.olegcherednik.zip4jvm.view.LocalFileHeaderView;

import java.io.PrintStream;
import java.nio.charset.Charset;

/**
 * @author Oleg Cherednik
 * @since 27.10.2019
 */
@Builder
public class ZipEntryView {

    private final int pos;
    private final LocalFileHeader localFileHeader;
    private final Diagnostic.ZipEntryBlock.LocalFileHeaderB diagLocalFileHeader;
    private final Diagnostic.ZipEntryBlock.EncryptionHeader encryptionHeader;
    private final DataDescriptor dataDescriptor;
    private final Block blockDataDescriptor;
    private final Charset charset;
    private final String prefix;

    public void print(PrintStream out) {
        printLocalFileHeader(out);
        out.println();
        printEncryptionHeader(out);
        out.println();
        printDataDescriptor(out);
    }

    private void printLocalFileHeader(PrintStream out) {
        LocalFileHeaderView.builder()
                           .localFileHeader(localFileHeader)
                           .diagLocalFileHeader(diagLocalFileHeader)
                           .pos(pos)
                           .charset(charset)
                           .offs(prefix.length())
                           .columnWidth(52).build().print(out);
    }

    private void printEncryptionHeader(PrintStream out) {
        EncryptionHeaderView.builder()
                            .encryptionHeader(encryptionHeader)
                            .pos(pos)
                            .charset(charset)
                            .prefix(prefix).build().print(out);
    }

    private void printDataDescriptor(PrintStream out) {
        DataDescriptorView.builder()
                          .dataDescriptor(dataDescriptor)
                          .block(blockDataDescriptor)
                          .pos(pos)
                          .offs(prefix.length())
                          .columnWidth(52).build().print(out);
    }

}
