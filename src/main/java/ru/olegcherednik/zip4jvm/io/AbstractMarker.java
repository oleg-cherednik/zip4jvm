package ru.olegcherednik.zip4jvm.io;

import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Oleg Cherednik
 * @since 12.10.2019
 */
public abstract class AbstractMarker implements Marker {

    private final Map<String, Long> map = new HashMap<>();
    private long tic;

    protected final void incTic(long inc) {
        tic += inc;
    }

    @Override
    public final void mark(String id) {
        map.put(id, tic);
    }

    @Override
    public final long getMark(String id) {
        if (map.containsKey(id))
            return map.get(id);
        throw new Zip4jvmException("Cannot find mark: " + id);
    }

    @Override
    public final long getWrittenBytesAmount(String id) {
        return tic - map.getOrDefault(id, 0L);
    }

}
