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
package ru.olegcherednik.zip4jvm.io;

import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Oleg Cherednik
 * @since 12.10.2019
 */
public class BaseMarker implements Marker {

    private final Map<String, Long> map = new HashMap<>();
    private long tic;

    public final void incTic() {
        tic++;
    }

    public final void incTic(long inc) {
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
    public final long getSize(String id) {
        return tic - map.getOrDefault(id, 0L);
    }

}
