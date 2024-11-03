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

import ru.olegcherednik.zip4jvm.io.ByteOrder;
import ru.olegcherednik.zip4jvm.io.Marker;

import org.apache.commons.lang3.ArrayUtils;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;

/**
 * This interface describes ability to write a data primitives like
 * <tt>BYTE</tt>, <tt>WORD</tt>, <tt>DWORD</tt> etc. to an abstract output
 * resource. This resource is not defined here, it should be defined in the
 * subclasses.
 * <p>
 * In case the {@link OutputStream} is <tt>an output stream of bytes</tt>, this
 * interface can be treated as <tt>on output stream of data primitives</tt>.
 *
 * @author Oleg Cherednik
 * @since 03.08.2019
 */
public abstract class DataOutput implements Marker, Closeable {

    public abstract ByteOrder getByteOrder();

    public abstract long getRelativeOffs();

    public abstract void writeByte(int val) throws IOException;

    public void writeWordSignature(int sig) throws IOException {
        writeWord(sig);
    }

    public void writeDwordSignature(int sig) throws IOException {
        writeDword(sig);
    }

    public abstract void writeWord(int val) throws IOException;

    public abstract void writeDword(long val) throws IOException;

    public abstract void writeQword(long val) throws IOException;

    public void writeBytes(byte... buf) throws IOException {
        if (ArrayUtils.isNotEmpty(buf))
            write(buf, 0, buf.length);
    }

    public abstract void write(byte[] buf, int offs, int len);

    public int getDiskNo() {
        return 0;
    }

    public abstract void flush() throws IOException;

}
