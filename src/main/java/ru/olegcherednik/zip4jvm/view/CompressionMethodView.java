package ru.olegcherednik.zip4jvm.view;

import ru.olegcherednik.zip4jvm.model.CompressionMethod;
import ru.olegcherednik.zip4jvm.model.GeneralPurposeFlag;

import java.io.PrintStream;

/**
 * @author Oleg Cherednik
 * @since 15.10.2019
 */
public class CompressionMethodView extends View {

    private final CompressionMethod compressionMethod;
    private final GeneralPurposeFlag generalPurposeFlag;

    public CompressionMethodView(CompressionMethod compressionMethod, GeneralPurposeFlag generalPurposeFlag, int offs, int columnWidth) {
        super(offs, columnWidth);
        this.compressionMethod = compressionMethod;
        this.generalPurposeFlag = generalPurposeFlag;
    }

    @Override
    public void print(PrintStream out) {
        printLine(out, String.format("compression method (%02d):", compressionMethod.getCode()), compressionMethod);

        if (compressionMethod == CompressionMethod.FILE_IMPLODED) {
            printLine(out, "  size of sliding dictionary (implosion):", generalPurposeFlag.getSlidingDictionarySize());
            printLine(out, "  number of Shannon-Fano trees (implosion):", generalPurposeFlag.getShannonFanoTreesNumber());
        } else if (compressionMethod == CompressionMethod.LZMA)
            printLine(out, "  end-of-stream (EOS) marker:", generalPurposeFlag.isEosMarker() ? "yes" : "no");
        else if (compressionMethod == CompressionMethod.DEFLATE || compressionMethod == CompressionMethod.FILE_ENHANCED_DEFLATED)
            printLine(out, "  compression sub-type (deflation):", generalPurposeFlag.getCompressionLevel());
    }

}
