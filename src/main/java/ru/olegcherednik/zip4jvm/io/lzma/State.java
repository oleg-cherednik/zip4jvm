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
package ru.olegcherednik.zip4jvm.io.lzma;

/**
 * @author Oleg Cherednik
 * @since 14.02.2020
 */
final class State {

    public static final int STATES = 12;

    private static final int LIT_STATES = 7;
    private static final int LIT_LIT = 0;
    private static final int SHORTREP_LIT_LIT = 3;
    private static final int LIT_MATCH = 7;
    private static final int LIT_LONGREP = 8;
    private static final int LIT_SHORTREP = 9;
    private static final int NONLIT_MATCH = 10;
    private static final int NONLIT_REP = 11;

    private int state = LIT_LIT;

    public int get() {
        return state;
    }

    public void set(State state) {
        this.state = state.state;
    }

    public void updateLiteral() {
        if (state <= SHORTREP_LIT_LIT)
            state = LIT_LIT;
        else if (state <= LIT_SHORTREP)
            state -= 3;
        else
            state -= 6;
    }

    public void updateMatch() {
        state = state < LIT_STATES ? LIT_MATCH : NONLIT_MATCH;
    }

    public void updateLongRep() {
        state = state < LIT_STATES ? LIT_LONGREP : NONLIT_REP;
    }

    public void updateShortRep() {
        state = state < LIT_STATES ? LIT_SHORTREP : NONLIT_REP;
    }

    public boolean isLiteral() {
        return state < LIT_STATES;
    }
}
