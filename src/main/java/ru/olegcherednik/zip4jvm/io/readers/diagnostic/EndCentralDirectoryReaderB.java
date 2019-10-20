package ru.olegcherednik.zip4jvm.io.readers.diagnostic;

import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.io.readers.EndCentralDirectoryReader;
import ru.olegcherednik.zip4jvm.model.EndCentralDirectory;
import ru.olegcherednik.zip4jvm.model.diagnostic.Block;
import ru.olegcherednik.zip4jvm.model.diagnostic.Diagnostic;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 20.10.2019
 */
public class EndCentralDirectoryReaderB extends EndCentralDirectoryReader {

    public EndCentralDirectoryReaderB(Function<Charset, Charset> charsetCustomizer) {
        super(charsetCustomizer);
    }

    @Override
    public EndCentralDirectory read(DataInput in) throws IOException {
        return Block.foo(in, Diagnostic.getInstance().getEndCentralDirectory(), () -> super.read(in));
    }

}
