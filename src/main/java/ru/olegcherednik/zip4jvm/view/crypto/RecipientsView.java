package ru.olegcherednik.zip4jvm.view.crypto;

import org.apache.commons.collections4.CollectionUtils;
import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeader;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.view.BaseView;

import java.io.PrintStream;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Oleg Cherednik
 * @since 19.03.2020
 */
public final class RecipientsView extends BaseView {

    private final List<DecryptionHeader.Recipient> recipients;
    private final Block block;

    public static RecipientsView.Builder builder() {
        return new RecipientsView.Builder();
    }

    private RecipientsView(RecipientsView.Builder builder) {
        super(builder.offs, builder.columnWidth, builder.totalDisks);
        recipients = builder.recipients;
        block = builder.block;
    }

    @Override
    public boolean print(PrintStream out) {
        printValueWithLocation1(out, "recipients:", block);
        printSize(out, recipients.size(), block);
        return false;
    }

    private void printSize(PrintStream out, int total, Block block) {
        if (total == 1)
            printLine(out, "  - size:", String.format("%d bytes (1 recipient)", block.getSize()));
        else
            printLine(out, "  - size:", String.format("%d bytes (%d recipients)", block.getSize(), total));
    }

    public static final class Builder {

        private List<DecryptionHeader.Recipient> recipients;
        private Block block;
        private int offs;
        private int columnWidth;
        private long totalDisks;

        public RecipientsView build() {
            Objects.requireNonNull(recipients, "'recipients' must not be null");
            Objects.requireNonNull(block, "'block' must not be null");
            return new RecipientsView(this);
        }

        public RecipientsView.Builder recipients(List<DecryptionHeader.Recipient> recipients) {
            this.recipients = CollectionUtils.isEmpty(recipients) ? Collections.emptyList() : Collections.unmodifiableList(recipients);
            return this;
        }

        public RecipientsView.Builder block(Block block) {
            this.block = block;
            return this;
        }

        public RecipientsView.Builder position(int offs, int columnWidth, long totalDisks) {
            this.offs = offs;
            this.columnWidth = columnWidth;
            this.totalDisks = totalDisks;
            return this;
        }
    }
}
