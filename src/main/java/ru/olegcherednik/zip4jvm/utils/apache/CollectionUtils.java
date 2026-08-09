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
package ru.olegcherednik.zip4jvm.utils.apache;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.Map;

/**
 * This is a part of `Apache's commons-collection4`. I am not include lib itself, because it weights over 2MB.
 *
 * @author Oleg Cherednik
 * @since 13.07.2026
 */
@SuppressWarnings("PMD.SimplifyBooleanReturns")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CollectionUtils {

    /**
     * Null-safe check if the specified collection is empty. {@literal null} returns true.
     *
     * @param coll the collection to check, may be {@literal null}
     * @return true if empty or {@literal null}
     */
    public static boolean isEmpty(Collection<?> coll) {
        return coll == null || coll.isEmpty();
    }

    /**
     * Null-safe check if the specified collection is not empty. {@literal null} returns false.
     *
     * @param coll the collection to check, may be {@literal null}
     * @return true if non-null and non-empty
     */
    public static boolean isNotEmpty(Collection<?> coll) {
        return !isEmpty(coll);
    }

    /**
     * Null-safe check if the specified map is empty. {@literal null} returns true.
     *
     * @param map the map to check, may be {@literal null}
     * @return true if empty or {@literal null}
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

}
