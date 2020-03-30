package ru.olegcherednik.zip4jvm.io.readers.block.crypto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeader;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.readers.DecryptionHeaderReader;
import ru.olegcherednik.zip4jvm.model.block.crypto.DecryptionHeaderBlock;

import java.io.IOException;
import java.util.List;

/**
 * @author Oleg Cherednik
 * @since 24.02.2020
 */
@Getter
@RequiredArgsConstructor
public class BlockDecryptionHeaderReader extends DecryptionHeaderReader {

    private final DecryptionHeaderBlock decryptionHeaderBlock = new DecryptionHeaderBlock();

    @Override
    public DecryptionHeader read(DataInput in) throws IOException {
        return decryptionHeaderBlock.calc(in, () -> super.read(in));
    }

    @Override
    protected List<DecryptionHeader.Recipient> readRecipients(long total, int hashSize, DataInput in) throws IOException {
        return decryptionHeaderBlock.getRecipientsBlock().calc(in, () -> super.readRecipients(total, hashSize, in));
    }

}
