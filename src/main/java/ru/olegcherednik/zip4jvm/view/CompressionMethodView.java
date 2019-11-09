package ru.olegcherednik.zip4jvm.view;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.olegcherednik.zip4jvm.model.CompressionMethod;
import ru.olegcherednik.zip4jvm.model.GeneralPurposeFlag;

import java.io.PrintStream;

/**
 * @author Oleg Cherednik
 * @since 15.10.2019
 */
public final class CompressionMethodView extends View {

    private final CompressionMethod compressionMethod;
    private final GeneralPurposeFlag generalPurposeFlag;

    public static Builder builder() {
        return new Builder();
    }

    private CompressionMethodView(Builder builder) {
        super(builder.offs, builder.columnWidth);
        compressionMethod = builder.compressionMethod;
        generalPurposeFlag = builder.generalPurposeFlag;
    }

    @Override
    public void print(PrintStream out) {
        printLine(out, String.format("compression method (%02d):", compressionMethod.getCode()), compressionMethod.getTitle());

        if (compressionMethod == CompressionMethod.FILE_IMPLODED) {
            printLine(out, "  size of sliding dictionary (implosion):", generalPurposeFlag.getSlidingDictionarySize().getTitle());
            printLine(out, "  number of Shannon-Fano trees (implosion):", generalPurposeFlag.getShannonFanoTreesNumber().getTitle());
        } else if (compressionMethod == CompressionMethod.LZMA)
            printLine(out, "  end-of-stream (EOS) marker:", generalPurposeFlag.isEosMarker() ? "yes" : "no");
        else if (compressionMethod == CompressionMethod.DEFLATE || compressionMethod == CompressionMethod.FILE_ENHANCED_DEFLATED)
            printLine(out, "  compression sub-type (deflation):", generalPurposeFlag.getCompressionLevel().getTitle());
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Builder {

        private CompressionMethod compressionMethod;
        private GeneralPurposeFlag generalPurposeFlag;
        private int offs;
        private int columnWidth;

        public CompressionMethodView build() {
            return new CompressionMethodView(this);
        }

        public Builder compressionMethod(CompressionMethod compressionMethod) {
            this.compressionMethod = compressionMethod;
            return this;
        }

        public Builder generalPurposeFlag(GeneralPurposeFlag generalPurposeFlag) {
            this.generalPurposeFlag = generalPurposeFlag;
            return this;
        }

        public Builder offs(int offs) {
            this.offs = offs;
            return this;
        }

        public Builder columnWidth(int columnWidth) {
            this.columnWidth = columnWidth;
            return this;
        }
    }


}
