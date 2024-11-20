/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
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
package ru.olegcherednik.zip4jvm.io.in.data;

import ru.olegcherednik.zip4jvm.io.BaseMarker;
import ru.olegcherednik.zip4jvm.io.in.data.xxx.RandomAccessDataInput;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Oleg Cherednik
 * @since 11.11.2024
 */
public abstract class MarkerDataInput extends InputStream implements RandomAccessDataInput {

    private final BaseMarker marker = new BaseMarker();

    // ---------- RandomAccessDataInput ----------

    @Override
    public void seek(String id) throws IOException {
        seek(getMark(id));
    }

    // ---------- Marker ----------

    @Override
    public void mark(String id) {
        marker.setTic(getAbsOffs());
        marker.mark(id);
    }

    @Override
    public final long getMark(String id) {
        return marker.getMark(id);
    }

    @Override
    public final long getMarkSize(String id) {
        marker.setTic(getAbsOffs());
        return marker.getMarkSize(id);
    }

}
