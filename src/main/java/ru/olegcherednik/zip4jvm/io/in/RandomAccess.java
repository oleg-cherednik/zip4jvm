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
package ru.olegcherednik.zip4jvm.io.in;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 26.01.2020
 */
public interface RandomAccess {

    // TODO looks like when delegate is encryption (e.g. AES), then it's mandatory not skip, but read bytes to keep counters up to date

    /**
     * @return actual skipped bytes
     */
    long skip(long bytes) throws IOException;

    /**
     * Move pointer to the given {@code absoluteOffs}. In case of {@code absoluteOffs} is out of range, than
     * current offs will not be changed.
     *
     * @param absoluteOffs absolute offs
     */
    void seek(long absoluteOffs) throws IOException;

    void backward(int bytes) throws IOException;
}
