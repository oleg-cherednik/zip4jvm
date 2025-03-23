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
package ru.olegcherednik.zip4jvm.crypto.aes;

/**
 * @author Oleg Cherednik
 * @since 24.03.2025
 */
//@Test
public class WinZipCipherTest {

    //    public void shouldUpdateIv() throws Throwable {
    //        AesEngine engine = new AesEngine();
    //        assertThat(getIv(engine)).isEqualTo(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });
    //
    //        ivUpdate(engine);
    //        assertThat(getIv(engine)).isEqualTo(new byte[] { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });
    //
    //        ivUpdate(engine);
    //        assertThat(getIv(engine)).isEqualTo(new byte[] { 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });
    //
    //        setIv(engine, new byte[] { -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });
    //        ivUpdate(engine);
    //        assertThat(getIv(engine)).isEqualTo(new byte[] { 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });
    //
    //        setIv(engine, new byte[] { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 });
    //        ivUpdate(engine);
    //        assertThat(getIv(engine)).isEqualTo(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });
    //    }

    //    private static byte[] getIv(WinZipAesFactory engine) throws NoSuchFieldException, IllegalAccessException {
    //        return ReflectionUtils.getFieldValue(engine, "iv");
    //    }

    //    private static void ivUpdate(WinZipAesFactory engine) throws Throwable {
    //        ReflectionUtils.invokeMethod(engine, "ivUpdate");
    //    }

    //    private static void setIv(WinZipAesFactory engine, byte[] iv) throws Exception, Exception {
    //        ReflectionUtils.setFieldValue(engine, "iv", iv);
    //    }

}
