package ru.olegcherednik.zip4jvm.view;

import lombok.Builder;
import ru.olegcherednik.zip4jvm.model.CompressionMethod;
import ru.olegcherednik.zip4jvm.model.GeneralPurposeFlag;

import java.io.PrintStream;

/**
 * @author Oleg Cherednik
 * @since 15.10.2019
 */
@Builder
public class CompressionMethodView {

    private final CompressionMethod compressionMethod;
    private final GeneralPurposeFlag generalPurposeFlag;
    private final String prefix;

    public void print(PrintStream out) {
        out.format("%scompression method (%02d):                        %s\n", prefix, compressionMethod.getCode(), compressionMethod.getTitle());

        if (compressionMethod == CompressionMethod.FILE_IMPLODED) {
            out.format("%s  size of sliding dictionary (implosion):       %s\n", prefix,
                    generalPurposeFlag.getSlidingDictionarySize().getTitle());
            out.format("%s  number of Shannon-Fano trees (implosion):     %s\n", prefix,
                    generalPurposeFlag.getShannonFanoTreesNumber().getTitle());
        } else if (compressionMethod == CompressionMethod.DEFLATE || compressionMethod == CompressionMethod.FILE_ENHANCED_DEFLATED)
            out.format("%s  compression sub-type (deflation):             %s\n", prefix,
                    generalPurposeFlag.getCompressionLevel().getTitle());
        else if (compressionMethod == CompressionMethod.LZMA)
            out.format("%s  end-of-stream (EOS) marker:                   %s\n", prefix, generalPurposeFlag.isEosMarker() ? "yes" : "no");
    }

}
