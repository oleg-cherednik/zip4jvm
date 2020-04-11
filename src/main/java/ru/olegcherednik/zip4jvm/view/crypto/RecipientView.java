package ru.olegcherednik.zip4jvm.view.crypto;

import ru.olegcherednik.zip4jvm.crypto.strong.Recipient;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.view.BaseView;
import ru.olegcherednik.zip4jvm.view.ByteArrayHexView;
import ru.olegcherednik.zip4jvm.view.SizeView;

import java.io.PrintStream;
import java.util.Objects;

/**
 * @author Oleg Cherednik
 * @since 30.03.2020
 */
final class RecipientView extends BaseView {

    private final int num;
    private final Recipient recipient;
    private final Block block;

    public static Builder builder() {
        return new Builder();
    }

    private RecipientView(Builder builder) {
        super(builder.offs, builder.columnWidth, builder.totalDisks);
        num = builder.num;
        recipient = builder.recipient;
        block = builder.block;
    }

    @Override
    public boolean print(PrintStream out) {
        printValueWithLocation1(out, String.format("#%d - location:", num + 1), block);
        printLine(out, "  size:", recipient.getSize());
        printHash(out);
        printSimpleKeyBlob(out);
        return true;
    }

    private void printHash(PrintStream out) {
        new SizeView("  public key hash:", recipient.getHash().length, offs, columnWidth).print(out);
        new ByteArrayHexView(recipient.getHash(), offs, columnWidth).print(out);
    }

    private void printSimpleKeyBlob(PrintStream out) {
        new SizeView("  simple key blob:", recipient.getSimpleKeyBlob().length, offs, columnWidth).print(out);
        new ByteArrayHexView(recipient.getSimpleKeyBlob(), offs, columnWidth).print(out);
    }

    public static final class Builder {

        private int num;
        private Recipient recipient;
        private Block block;
        private int offs;
        private int columnWidth;
        private long totalDisks;

        public RecipientView build() {
            Objects.requireNonNull(recipient, "'recipient' must not be null");
            Objects.requireNonNull(block, "'block' must not be null");
            return new RecipientView(this);
        }

        public Builder num(int num) {
            this.num = num;
            return this;
        }

        public Builder recipient(Recipient recipient) {
            this.recipient = recipient;
            return this;
        }

        public Builder block(Block block) {
            this.block = block;
            return this;
        }

        public Builder position(int offs, int columnWidth, long totalDisks) {
            this.offs = offs;
            this.columnWidth = columnWidth;
            this.totalDisks = totalDisks;
            return this;
        }
    }
}
