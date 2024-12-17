package ru.olegcherednik.zip4jvm.io.in;

import ru.olegcherednik.zip4jvm.io.BaseMarker;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 25.11.2024
 */
public abstract class BaseConsecutiveAccessDataInput extends BaseDataInput {

    private final BaseMarker marker = new BaseMarker();

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

    @Override
    public long getAbsOffs() {
        return 0;
    }

    @Override
    public long skip(long bytes) throws IOException {
        return 0;
    }

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        return 0;
    }

}
