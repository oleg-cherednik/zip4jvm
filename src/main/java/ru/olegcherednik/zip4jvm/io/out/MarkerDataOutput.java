/*
 * Copyright 2019 Oleg Cherednik (oleg.cherednik@gmail.com)
 *
 * Licensed under The Apache Software License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package ru.olegcherednik.zip4jvm.io.out;

import ru.olegcherednik.zip4jvm.io.BaseMarker;

/**
 * This class adds a marker to {@link DataOutput}. It's possible to mark
 * current position to retrieve it later or calculate distance in bytes passed
 * from the marked positon to current.
 *
 * @author Oleg Cherednik
 * @since 04.11.2024
 */
public abstract class MarkerDataOutput implements DataOutput {

    private final BaseMarker marker = new BaseMarker();
    private long absOffs;

    // ---------- WriteBuffer ----------

    @Override
    public void write(int b) {
        absOffs++;
    }

    // ---------- Marker ----------

    @Override
    public final void mark(String id) {
        marker.setAbsOffs(absOffs);
        marker.mark(id);
    }

    @Override
    public final long getMark(String id) {
        return marker.getMark(id);
    }

    @Override
    public final long getMarkSize(String id) {
        marker.setAbsOffs(absOffs);
        return marker.getMarkSize(id);
    }

}
