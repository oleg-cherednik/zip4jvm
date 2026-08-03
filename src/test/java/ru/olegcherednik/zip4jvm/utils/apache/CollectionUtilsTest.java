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

import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Oleg Cherednik
 * @since 15.07.2026
 */
@Test
@SuppressWarnings("ConstantValue")
public class CollectionUtilsTest {

    public void shouldRetrieveTrueWhenCollectionIsNullOrEmpty() {
        assertThat(CollectionUtils.isEmpty((Collection<?>) null)).isTrue();
        assertThat(CollectionUtils.isEmpty(Collections.emptyList())).isTrue();
    }

    public void shouldRetrieveFalseWhenCollectionIsNotEmpty() {
        assertThat(CollectionUtils.isEmpty(Collections.singletonList("one1"))).isFalse();
        assertThat(CollectionUtils.isEmpty(Arrays.asList("one1", "two1"))).isFalse();
    }

    public void shouldRetrieveTrueWhenCollectionIsNotEmpty() {
        assertThat(CollectionUtils.isNotEmpty(Collections.singletonList("one2"))).isTrue();
        assertThat(CollectionUtils.isNotEmpty(Arrays.asList("one2", "two2"))).isTrue();
    }

    public void shouldRetrieveFalseWhenCollectionIsNullOrEmpty() {
        assertThat(CollectionUtils.isNotEmpty(null)).isFalse();
        assertThat(CollectionUtils.isNotEmpty(Collections.emptyList())).isFalse();
    }

    public void shouldRetrieveTrueWhenMapIsNullOrEmpty() {
        assertThat(CollectionUtils.isEmpty((Map<?, ?>) null)).isTrue();
        assertThat(CollectionUtils.isEmpty(Collections.emptyMap())).isTrue();
    }

    public void shouldRetrieveFalseWhenMapIsNotEmpty() {
        assertThat(CollectionUtils.isEmpty(Collections.singletonMap("key", "value"))).isFalse();
    }

}
