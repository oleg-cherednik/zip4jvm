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
package ru.olegcherednik.zip4jvm.utils.apache;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.reflect.Array;

/**
 * @author Oleg Cherednik
 * @since 21.07.2026
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ArrayUtils {

    /**
     * An empty immutable {@code byte} array.
     */
    public static final byte[] EMPTY_BYTE_ARRAY = {};

    /**
     * An empty immutable {@code char} array.
     */
    public static final char[] EMPTY_CHAR_ARRAY = {};

    /**
     * An empty immutable {@link String} array.
     */
    public static final String[] EMPTY_STRING_ARRAY = {};

    /**
     * Tests whether an array of primitive bytes is empty or {@code null}.
     *
     * @param array the array to test
     * @return {@code true} if the array is empty or {@code null}
     * @since 2.1
     */
    public static boolean isEmpty(final byte[] array) {
        return isArrayEmpty(array);
    }

    /**
     * Tests whether an array of Objects is empty or {@code null}.
     *
     * @param array the array to test
     * @return {@code true} if the array is empty or {@code null}
     * @since 2.1
     */
    @SuppressWarnings("MethodCanBeVariableArityMethod")
    public static boolean isEmpty(final Object[] array) {
        return isArrayEmpty(array);
    }

    /**
     * Tests whether an array of primitive chars is empty or {@code null}.
     *
     * @param array the array to test
     * @return {@code true} if the array is empty or {@code null}
     * @since 2.1
     */
    public static boolean isEmpty(final char[] array) {
        return isArrayEmpty(array);
    }

    /**
     * Tests whether an array of primitive bytes is not empty and not {@code null}.
     *
     * @param array the array to test
     * @return {@code true} if the array is not empty and not {@code null}
     * @since 2.5
     */
    public static boolean isNotEmpty(final byte[] array) {
        return !isEmpty(array);
    }

    /**
     * Tests whether an array of Objects is not empty and not {@code null}.
     *
     * @param <T>   the component type of the array
     * @param array the array to test
     * @return {@code true} if the array is not empty and not {@code null}
     * @since 2.5
     */
    @SuppressWarnings("MethodCanBeVariableArityMethod")
    public static <T> boolean isNotEmpty(final T[] array) {
        return !isEmpty(array);
    }

    /**
     * Checks if an array is empty or {@code null}.
     *
     * @param array the array to test
     * @return {@code true} if the array is empty or {@code null}
     */
    private static boolean isArrayEmpty(final Object array) {
        return getLength(array) == 0;
    }

    /**
     * Gets the length of the specified array.
     * This method can deal with {@link Object} arrays and with primitive arrays.
     * <p>
     * If the input array is {@code null}, {@code 0} is returned.
     * </p>
     * <pre>
     * ArrayUtils.getLength(null)            = 0
     * ArrayUtils.getLength([])              = 0
     * ArrayUtils.getLength([null])          = 1
     * ArrayUtils.getLength([true, false])   = 2
     * ArrayUtils.getLength([1, 2, 3])       = 3
     * ArrayUtils.getLength(["a", "b", "c"]) = 3
     * </pre>
     *
     * @param array the array to retrieve the length from, may be {@code null}.
     * @return The length of the array, or {@code 0} if the array is {@code null}
     * @throws IllegalArgumentException if the object argument is not an array.
     * @since 2.1
     */
    public static int getLength(final Object array) {
        return array != null ? Array.getLength(array) : 0;
    }

    /**
     * Clones an array or returns {@code null}.
     * <p>
     * This method returns {@code null} for a {@code null} input array.
     * </p>
     *
     * @param array the array to clone, may be {@code null}
     * @return the cloned array, {@code null} if {@code null} input
     */
    public static byte[] clone(final byte[] array) {
        return array != null ? array.clone() : null;
    }

    /**
     * Clones an array or returns {@code null}.
     * <p>
     * This method returns {@code null} for a {@code null} input array.
     * </p>
     *
     * @param array the array to clone, may be {@code null}
     * @return the cloned array, {@code null} if {@code null} input
     */
    public static char[] clone(final char[] array) {
        return array != null ? array.clone() : null;
    }

}
