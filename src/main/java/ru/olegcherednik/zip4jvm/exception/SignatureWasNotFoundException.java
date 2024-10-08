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

import ru.olegcherednik.zip4jvm.view.BaseView;

import lombok.Getter;

/**
 * @author Oleg Cherednik
 * @since 04.12.2019
 */
@Getter
public class SignatureWasNotFoundException extends Zip4jvmException {

    private static final long serialVersionUID = -8774784649137793410L;

    private final int signature;
    private final String name;
    private final long offs;

    public SignatureWasNotFoundException(int signature, String name) {
        super(String.format("Signature %s (%s) was not found", BaseView.signature(signature), name));
        this.signature = signature;
        this.name = name;
        offs = -1;
    }

    public SignatureWasNotFoundException(int signature, String name, long offs) {
        super(String.format("Signature %s (%s) was not found: (0x%08X)", BaseView.signature(signature), name, offs));
        this.signature = signature;
        this.name = name;
        this.offs = offs;
    }

}
