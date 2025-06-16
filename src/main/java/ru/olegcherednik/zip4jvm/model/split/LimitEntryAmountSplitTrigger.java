package ru.olegcherednik.zip4jvm.model.split;

import lombok.RequiredArgsConstructor;

import java.util.function.LongSupplier;

/**
 * @author Oleg Cherednik
 * @since 15.06.2025
 */
@RequiredArgsConstructor
public class LimitEntryAmountSplitTrigger implements SplitTrigger {

    private final long limitEntryAmount;
    private final LongSupplier entryNo;

    // ---------- SplitTrigger ----------

    @Override
    public boolean doSplit(long offs, int diskNo) {
        long entryNo = this.entryNo.getAsLong();
        return entryNo == (diskNo + 1) * limitEntryAmount;
    }

    // ---------- Object ----------

    @Override
    public String toString() {
        return "limitEntryAmount: " + limitEntryAmount;
    }

}
