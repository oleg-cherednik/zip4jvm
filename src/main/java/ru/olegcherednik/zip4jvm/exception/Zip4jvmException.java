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
package ru.olegcherednik.zip4jvm.exception;

import lombok.Getter;

@Getter
@SuppressWarnings("ClassWithTooManyConstructors")
public class Zip4jvmException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final ErrorCode code;

    public Zip4jvmException() {
        this(null, ErrorCode.UNKNOWN);
    }

    public Zip4jvmException(String message) {
        this(message, ErrorCode.UNKNOWN);
    }

    public Zip4jvmException(String format, Object... args) {
        this(String.format(format, args), ErrorCode.UNKNOWN);
    }

    public Zip4jvmException(String message, Throwable cause) {
        this(message, ErrorCode.UNKNOWN, cause);
    }

    public Zip4jvmException(String message, ErrorCode code) {
        this(message, code, null);
    }

    public Zip4jvmException(Throwable cause) {
        this(ErrorCode.UNKNOWN, cause);
    }

    public Zip4jvmException(ErrorCode code, Throwable cause) {
        this(cause.getMessage(), code, cause);
    }

    public Zip4jvmException(String message, ErrorCode code, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

}
