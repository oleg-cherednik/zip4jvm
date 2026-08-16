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
package ru.olegcherednik.zip4jvm;

import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * @author Oleg Cherednik
 * @since 06.09.2025
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CallerInfo {

    private static final Class<?> UTIL_CLASS = Zip4jvmSuite.class;

    @SuppressWarnings("StaticCollection")
    private static final Set<String> EXCLUDE_CLASSES = Set.of(
            UTIL_CLASS.getName(),
            BaseTest.class.getName(),
            DirRoot.class.getName());

    public static String getCallerMethodName() {
        return getCallerElement(UTIL_CLASS).getMethodName();
    }

    public static Class<?> getCallerClass() {
        return Quietly.doRuntime(() -> Class.forName(getCallerElement(UTIL_CLASS).getClassName()));
    }

    private static StackTraceElement getCallerElement(Class<?> utilClass) {
        System.out.println("getCallerElement");
        boolean check = false;

        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            String className = element.getClassName();
            System.out.println(className);

            if (check) {
                if (!EXCLUDE_CLASSES.contains(className))
                    return element;
            } else if (utilClass.getName().equals(className))
                check = true;
        }

        throw new Zip4jvmException("Cannot detect caller class element");
    }

}
