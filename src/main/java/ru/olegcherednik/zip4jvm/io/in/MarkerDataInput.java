package ru.olegcherednik.zip4jvm.io.in;

import ru.olegcherednik.zip4jvm.io.BaseMarker;

/**
 * @author Oleg Cherednik
 * @since 20.12.2024
 */
public abstract class MarkerDataInput extends BaseDataInput {

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

}
