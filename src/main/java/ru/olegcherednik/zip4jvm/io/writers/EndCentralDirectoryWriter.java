package ru.olegcherednik.zip4jvm.io.writers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.io.out.DataOutput;
import ru.olegcherednik.zip4jvm.model.EndCentralDirectory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author Oleg Cherednik
 * @since 10.04.2019
 */
@RequiredArgsConstructor
final class EndCentralDirectoryWriter implements Writer {

    @NonNull
    private final EndCentralDirectory endCentralDirectory;

    @Override
    public void write(@NonNull DataOutput out) throws IOException {
        byte[] comment = endCentralDirectory.getComment(StandardCharsets.UTF_8);

        out.writeDwordSignature(EndCentralDirectory.SIGNATURE);
        out.writeWord(endCentralDirectory.getTotalDisks());
        out.writeWord(endCentralDirectory.getMainDisk());
        out.writeWord(endCentralDirectory.getDiskEntries());
        out.writeWord(endCentralDirectory.getTotalEntries());
        out.writeDword(endCentralDirectory.getCentralDirectorySize());
        out.writeDword(endCentralDirectory.getCentralDirectoryOffs());
        out.writeWord(comment.length);
        out.writeBytes(comment);
    }

}
