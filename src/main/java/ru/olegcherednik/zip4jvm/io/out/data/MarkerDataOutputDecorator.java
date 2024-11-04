package ru.olegcherednik.zip4jvm.io.out.data;

import ru.olegcherednik.zip4jvm.io.BaseMarker;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 04.11.2024
 */
public abstract class MarkerDataOutputDecorator extends DataOutput {

    private final BaseMarker marker = new BaseMarker();

    // ---------- OutputStream ----------

    @Override
    public void write(int b) throws IOException {
        marker.incTic();
    }

    // ---------- Marker ----------

    @Override
    public final void mark(String id) {
        marker.mark(id);
    }

    @Override
    public final long getMark(String id) {
        return marker.getMark(id);
    }

    @Override
    public final long getWrittenBytesAmount(String id) {
        return marker.getWrittenBytesAmount(id);
    }

}
