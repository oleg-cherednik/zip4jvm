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
package ru.olegcherednik.zip4jvm.utils.quitely;

import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.utils.quitely.functions.ByteSupplierWithException;
import ru.olegcherednik.zip4jvm.utils.quitely.functions.IntSupplierWithException;
import ru.olegcherednik.zip4jvm.utils.quitely.functions.SupplierWithException;
import ru.olegcherednik.zip4jvm.utils.quitely.functions.TaskWithException;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author Oleg Cherednik
 * @since 15.04.2023
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Quietly {

    public static <T> T doRuntime(SupplierWithException<T> supplier) {
        try {
            return supplier.get();
        } catch (Zip4jvmException e) {
            throw e;
        } catch (Exception e) {
            throw new Zip4jvmException(e);
        }
    }

    public static int doRuntime(IntSupplierWithException supplier) {
        try {
            return supplier.getAsInt();
        } catch (Zip4jvmException e) {
            throw e;
        } catch (Exception e) {
            throw new Zip4jvmException(e);
        }
    }

    public static byte doRuntime(ByteSupplierWithException supplier) {
        try {
            return supplier.getAsByte();
        } catch (Zip4jvmException e) {
            throw e;
        } catch (Exception e) {
            throw new Zip4jvmException(e);
        }
    }

    public static void doRuntime(TaskWithException task) {
        try {
            task.run();
        } catch (Zip4jvmException e) {
            throw e;
        } catch (Exception e) {
            throw new Zip4jvmException(e);
        }
    }

}
