package ru.olegcherednik.zip4jvm.view.zip64;

import lombok.Builder;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.block.Diagnostic;

import java.io.PrintStream;
import java.nio.charset.Charset;

/**
 * @author Oleg Cherednik
 * @since 14.10.2019
 */
@Builder
public final class Zip64View {

    private final Zip64 zip64;
    private final Diagnostic.Zip64 diagZip64;
    private final Charset charset;
    private final String prefix;

    public void print(PrintStream out) {
        printEndCentralDirectorLocator(out);
        out.println();
        printEndCentralDirectory(out);
    }

    private void printEndCentralDirectorLocator(PrintStream out) {
        EndCentralDirectoryLocatorView.builder()
                                      .locator(zip64.getEndCentralDirectoryLocator())
                                      .block(diagZip64.getEndCentralDirectoryLocator())
                                      .offs(prefix.length())
                                      .columnWidth(52).build().print(out);
    }

    private void printEndCentralDirectory(PrintStream out) {
        EndCentralDirectoryView.builder()
                               .endCentralDirectory(zip64.getEndCentralDirectory())
                               .block(diagZip64.getEndCentralDirectory())
                               .offs(prefix.length())
                               .columnWidth(52).build().print(out);
    }

}
