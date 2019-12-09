package ru.olegcherednik.zip4jvm.view.centraldirectory;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.ArrayUtils;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.view.ByteArrayHexView;
import ru.olegcherednik.zip4jvm.view.View;

import java.io.PrintStream;
import java.util.Optional;

/**
 * @author Oleg Cherednik
 * @since 18.10.2019
 */
public final class DigitalSignatureView extends View {

    private final CentralDirectory.DigitalSignature digitalSignature;
    private final Block block;

    public static Builder builder() {
        return new Builder();
    }

    private DigitalSignatureView(Builder builder) {
        super(builder.offs, builder.columnWidth);
        digitalSignature = builder.digitalSignature;
        block = builder.block;
    }

    @Override
    public boolean print(PrintStream out) {
        if (digitalSignature == null || ArrayUtils.isEmpty(digitalSignature.getSignatureData()))
            return false;

        printTitle(out, CentralDirectory.DigitalSignature.SIGNATURE, "Digital signature", block);
        printData(out);

        return true;
    }

    private boolean printData(PrintStream out) {
        return ByteArrayHexView.builder()
                               .data(digitalSignature.getSignatureData())
                               .position(offs, columnWidth).build().print(out);
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Builder {

        private CentralDirectory.DigitalSignature digitalSignature;
        private Block block = Block.NULL;
        private int offs;
        private int columnWidth;

        public DigitalSignatureView build() {
            return new DigitalSignatureView(this);
        }

        public Builder digitalSignature(CentralDirectory.DigitalSignature digitalSignature) {
            this.digitalSignature = digitalSignature;
            return this;
        }

        public Builder block(Block block) {
            this.block = Optional.ofNullable(block).orElse(Block.NULL);
            return this;
        }

        public Builder position(int offs, int columnWidth) {
            this.offs = offs;
            this.columnWidth = columnWidth;
            return this;
        }
    }

}
