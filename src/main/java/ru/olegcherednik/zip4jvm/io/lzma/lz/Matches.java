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
package ru.olegcherednik.zip4jvm.io.lzma.lz;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Oleg Cherednik
 * @since 14.02.2020
 */
@Getter
public final class Matches {

    private final int[] len;
    private final int[] dist;
    @Setter
    private int count;

    Matches(int maxCount) {
        len = new int[maxCount];
        dist = new int[maxCount];
    }

    public void incCount() {
        count++;
    }

    public void decCount() {
        count--;
    }
}
