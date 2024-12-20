package ru.olegcherednik.zip4jvm.io.in.file.consecutive;

import ru.olegcherednik.zip4jvm.io.in.MarkerDataInput;

/**
 * @author Oleg Cherednik
 * @since 25.11.2024
 */
public abstract class BaseConsecutiveAccessDataInput extends MarkerDataInput implements ConsecutiveDataInput {

    private long absOffs;

    protected void incAbsOffs(long bytes) {
        absOffs += bytes;
    }

    // ---------- DataInput ----------

    @Override
    public long getAbsOffs() {
        return absOffs;
    }

}
