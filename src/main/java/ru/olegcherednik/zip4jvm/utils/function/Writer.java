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
package ru.olegcherednik.zip4jvm.utils.function;

import ru.olegcherednik.zip4jvm.io.out.DataOutput;

/**
 * This is an abstraction, that knows how to write itself to the given {@link DataOutput}.
 *
 * @author Oleg Cherednik
 * @since 15.09.2019
 */
@FunctionalInterface
public interface Writer {

    void write(DataOutput out);

}
