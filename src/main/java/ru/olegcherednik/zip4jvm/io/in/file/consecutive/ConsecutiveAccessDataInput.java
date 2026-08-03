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
package ru.olegcherednik.zip4jvm.io.in.file.consecutive;

import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

/**
 * This interface extends {@link DataInput} with adding ability consecutive
 * data access. It means that it's able to move forward only.
 *
 * @author Oleg Cherednik
 * @since 20.12.2024
 */
public interface ConsecutiveAccessDataInput extends DataInput {

    default long seekForward(long dstAbsOffs) {
        long absOffs = getAbsOffs();

        if (dstAbsOffs == absOffs)
            return 0;
        if (dstAbsOffs < absOffs)
            throw new Zip4jvmException("can't move backward");

        return Quietly.doRuntime(() -> skip(dstAbsOffs - absOffs));
    }

}
