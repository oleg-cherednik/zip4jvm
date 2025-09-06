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
package ru.olegcherednik.zip4jvm;

import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author Oleg Cherednik
 * @since 06.09.2025
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CallerInfo {

    public static String getCallerMethodName(Class<?> utilClass) {
        return getCallerElement(utilClass).getMethodName();
    }

    public static Class<?> getCallerClass(Class<?> utilClass) {
        try {
            return Class.forName(getCallerElement(utilClass).getClassName());
        } catch (ClassNotFoundException e) {
            throw new Zip4jvmException(e);
        }
    }

    private static StackTraceElement getCallerElement(Class<?> utilClass) {
        boolean check = false;

        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            String className = element.getClassName();

            if (check) {
                if (!utilClass.getName().equals(className))
                    return element;
            } else if (utilClass.getName().equals(className))
                check = true;
        }

        throw new Zip4jvmException("Cannot detect caller class element");
    }

}
