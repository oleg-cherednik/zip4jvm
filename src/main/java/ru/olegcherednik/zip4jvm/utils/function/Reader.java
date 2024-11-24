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
package ru.olegcherednik.zip4jvm.utils.function;

import ru.olegcherednik.zip4jvm.io.in.DataInput;

import java.io.IOException;

/**
 * This interface describes an abstract reader. Using given {@link  DataInput}
 * it can read an object of type <tt>T</tt>.
 *
 * @param <T> the type of results supplied by this reader
 * @author Oleg Cherednik
 * @since 20.12.2022
 */
@FunctionalInterface
public interface Reader<T> {

    T read(DataInput in) throws IOException;

}
