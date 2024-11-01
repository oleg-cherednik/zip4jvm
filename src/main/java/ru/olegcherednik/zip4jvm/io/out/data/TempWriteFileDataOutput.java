package ru.olegcherednik.zip4jvm.io.out.data;

import ru.olegcherednik.zip4jvm.io.out.file.ByteOrderOutputStream;

import lombok.AccessLevel;
import lombok.Getter;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 01.11.2024
 */
public class TempWriteFileDataOutput extends WriteFileDataOutput {

    @Getter(AccessLevel.PROTECTED)
    private final ByteOrderOutputStream out;

    public TempWriteFileDataOutput(Path zip) throws IOException {
        out = ByteOrderOutputStream.littleEndian(zip);
    }


}
