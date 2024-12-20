package ru.olegcherednik.zip4jvm.io.in.file.consecutive;

import ru.olegcherednik.zip4jvm.io.BaseMarker;
import ru.olegcherednik.zip4jvm.io.in.BaseDataInput;

import lombok.Getter;

/**
 * @author Oleg Cherednik
 * @since 25.11.2024
 */
public abstract class BaseConsecutiveAccessDataInput extends BaseDataInput implements ConsecutiveDataInput {

    private final BaseMarker marker = new BaseMarker();
    @Getter
    private long absOffs;

    protected void incAbsOffs(long bytes) {
        absOffs += bytes;
    }

    // ---------- Marker ----------

    @Override
    public void mark(String id) {
        marker.setOffs(getAbsOffs());
        marker.mark(id);
    }

    @Override
    public final long getMark(String id) {
        return marker.getMark(id);
    }

    @Override
    public final long getMarkSize(String id) {
        marker.setOffs(getAbsOffs());
        return marker.getMarkSize(id);
    }

}
