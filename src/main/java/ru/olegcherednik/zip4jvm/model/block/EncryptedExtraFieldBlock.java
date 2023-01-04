package ru.olegcherednik.zip4jvm.model.block;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.decompose.Utils;
import ru.olegcherednik.zip4jvm.model.ZipModel;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 04.01.2023
 */
@RequiredArgsConstructor
public class EncryptedExtraFieldBlock extends ExtraFieldBlock {

    private final byte[] buf;

    @Override
    public Block createRecordBlock() {
        return new Block() {
            @Override
            public void copyLarge(ZipModel zipModel, Path out) throws IOException {
                Utils.copyByteArray(out, buf, this);
            }
        };
    }

}
