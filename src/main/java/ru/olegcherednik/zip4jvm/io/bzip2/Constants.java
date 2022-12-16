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
package ru.olegcherednik.zip4jvm.io.bzip2;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author Oleg Cherednik
 * @since 12.04.2020
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class Constants {

    public static final int BASE_BLOCK_SIZE = 100_000;
    public static final int MAX_ALPHA_SIZE = 258;
    public static final int MAX_CODE_LEN = 23;
    public static final int RUNA = 0;
    public static final int RUNB = 1;
    public static final int N_GROUPS = 6;
    public static final int G_SIZE = 50;
    public static final int N_ITERS = 4;
    public static final int MAX_SELECTORS = 2 + (900000 / G_SIZE);
    public static final int NUM_OVERSHOOT_BYTES = 20;

}
