package com.cop.zip4j.io;

import lombok.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Oleg Cherednik
 * @since 03.08.2019
 */
public abstract class MarkDataOutputStream extends DataOutputStreamAdapter {

    private final Map<String, Long> map = new HashMap<>();

    protected MarkDataOutputStream(@NonNull DataOutput out) {
        super(out);
    }

    public void mark(String id) {
        map.put(id, getOffs());
    }

    public long getWrittenBytesAmount(String id) {
        return getOffs() - map.getOrDefault(id, 0L);
    }
}
