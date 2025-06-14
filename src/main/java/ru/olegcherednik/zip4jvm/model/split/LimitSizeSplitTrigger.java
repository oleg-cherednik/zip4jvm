package ru.olegcherednik.zip4jvm.model.split;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LimitSizeSplitTrigger implements SplitTrigger {

    private final long limitSize;

    @Override
    public boolean doSplit(long offs) {
        return limitSize - offs <= 0;
    }

    @Override
    public String toString() {
        return "limitSize: " + limitSize;
    }
}
