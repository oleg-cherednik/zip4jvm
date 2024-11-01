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
package ru.olegcherednik.zip4jvm.io.out.data;

import ru.olegcherednik.zip4jvm.io.out.file.ByteOrderOutputStream;
import ru.olegcherednik.zip4jvm.io.out.file.LittleEndianOutputStream;

import java.io.IOException;
import java.nio.file.Path;

/**
 * This is an adapter from {@link DataOutput} to {@link ByteOrderOutputStream}. Using this
 * adapter it's possible to write data primitives to the given file.
 * <p>
 * Method {@link WriteFileDataOutput#createFile(Path)} should be invoked before
 * writing anything to the output. {@link WriteFileDataOutput#out} can be
 * dynamically recreated pointing to another file.
 *
 * @author Oleg Cherednik
 * @since 11.02.2020
 */
public class WriteFileDataOutput extends BaseDataOutput {

    private ByteOrderOutputStream out;

    public final void createFile(Path zip) throws IOException {
        out = LittleEndianOutputStream.create(zip);
    }

    @Override
    public void fromLong(long val, byte[] buf, int offs, int len) {
        out.fromLong(val, buf, offs, len);
    }

    @Override
    public final long getRelativeOffs() {
        return out.getRelativeOffs();
    }

    @Override
    protected void writeInternal(byte[] buf, int offs, int len) throws IOException {
        out.write(buf, offs, len);
    }

    @Override
    public void close() throws IOException {
        out.close();
    }

    @Override
    public void flush() throws IOException {
        out.flush();
    }

    @Override
    public String toString() {
        return out.toString();
    }

}
