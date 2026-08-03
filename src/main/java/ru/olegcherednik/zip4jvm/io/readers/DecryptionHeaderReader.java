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
package ru.olegcherednik.zip4jvm.io.readers;

import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeader;
import ru.olegcherednik.zip4jvm.crypto.strong.Flag;
import ru.olegcherednik.zip4jvm.crypto.strong.HashAlgorithm;
import ru.olegcherednik.zip4jvm.crypto.strong.Recipient;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.utils.function.Reader;

import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.List;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.realBigZip64Check;

/**
 * @author Oleg Cherednik
 * @since 11.10.2019
 */
public class DecryptionHeaderReader implements Reader<DecryptionHeader> {

    private static final String MARKER_VERSION = "DecryptionHeaderReader.MARKER_VERSION";

    @Override
    @SuppressWarnings("VariableDeclarationUsageDistance")
    public DecryptionHeader read(DataInput in) {
        DecryptionHeader decryptionHeader = new DecryptionHeader();

        int ivSize = in.readWord();
        decryptionHeader.setIv(in.readBytes(ivSize));

        long size = in.readDword();
        in.mark(MARKER_VERSION);

        decryptionHeader.setVersion(in.readWord());
        decryptionHeader.setEncryptionAlgorithm(in.readWord());
        decryptionHeader.setBitLength(in.readWord());
        decryptionHeader.setFlags(Flag.parseCode(in.readWord()));

        int encryptedRandomDataSize = in.readWord();
        decryptionHeader.setEncryptedRandomData(in.readBytes(encryptedRandomDataSize));
        int recipientCount = (int) in.readDword();

        realBigZip64Check(recipientCount, "zip64.decryptionHeader.recipientCount");

        decryptionHeader.setHashAlgorithm(decryptionHeader.isPasswordKey() ? HashAlgorithm.NONE.getCode()
                                                                           : in.readWord());

        int hashSize = decryptionHeader.isPasswordKey() ? 0x0 : in.readWord();
        decryptionHeader.setRecipients(readRecipients(recipientCount, hashSize, in));
        int passwordValidationDataSize = in.readWord();
        decryptionHeader.setPasswordValidationData(in.readBytes(passwordValidationDataSize));

        if (in.getMarkSize(MARKER_VERSION) != size)
            throw new Zip4jvmException("DecryptionHeader size is incorrect");

        return decryptionHeader;
    }

    protected List<Recipient> readRecipients(int total, int hashSize, DataInput in) {
        return new Recipients(total, hashSize).read(in);
    }

    @RequiredArgsConstructor
    private static final class Recipients implements Reader<List<Recipient>> {

        private final int total;
        private final int hashSize;

        @Override
        public List<Recipient> read(DataInput in) {
            List<Recipient> recipients = new LinkedList<>();

            for (int i = 0; i < total; i++)
                recipients.add(createRecipient(in));

            return recipients;
        }

        Recipient createRecipient(DataInput in) {
            Recipient recipient = new Recipient();
            recipient.setSize(in.readWord());
            recipient.setHash(in.readBytes(hashSize));
            recipient.setSimpleKeyBlob(in.readBytes(recipient.getSize() - hashSize));
            return recipient;
        }
    }

}
