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

package net.lingala.zip4j.model;

import lombok.Getter;
import lombok.Setter;
import net.lingala.zip4j.util.InternalZipConstants;

@Getter
@Setter
public class DigitalSignature {

    // size:4 - header signature (0x06054b50)
    private final int signature = InternalZipConstants.DIGSIG;
    // size:2 - size of data (n)
    private int sizeOfData;
    // size:n - signature data
    private byte[] signatureData;

}
