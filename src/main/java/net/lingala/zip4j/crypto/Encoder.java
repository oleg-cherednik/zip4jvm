/*
 * Copyright 2010 Srikanth Reddy Lingala
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.lingala.zip4j.crypto;

import lombok.NonNull;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.SplitOutputStream;

import java.io.IOException;

@SuppressWarnings("MethodCanBeVariableArityMethod")
public interface Encoder {

    // TODO should nobe here
    Encoder NULL = new Encoder() {
        @Override
        public void encode(byte[] buf, int offs, int len) {
        }

        @Override
        public void write(@NonNull SplitOutputStream out) throws IOException {
        }
    };

    default void encode(byte[] buf) throws ZipException {
        encode(buf, 0, buf.length);
    }

    void encode(byte[] buf, int offs, int len);

    void write(@NonNull SplitOutputStream out) throws IOException;

}
