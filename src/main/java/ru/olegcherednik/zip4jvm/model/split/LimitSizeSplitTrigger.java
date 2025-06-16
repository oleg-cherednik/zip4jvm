package ru.olegcherednik.zip4jvm.model.split;

import lombok.RequiredArgsConstructor;

/**
 * @author Oleg Cherednik
 * @since 14.06.2025
 */
@RequiredArgsConstructor
public class LimitSizeSplitTrigger implements SplitTrigger {

    private final long limitSize;

    // ---------- SplitTrigger ----------

    @Override
    public boolean doSplit(long offs, int diskNo) {
        return limitSize - offs <= 0;
    }

    // ---------- Object ----------

    @Override
    public String toString() {
        return "limitSize: " + limitSize;
    }

}
