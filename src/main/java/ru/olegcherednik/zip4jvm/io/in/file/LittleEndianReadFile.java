package ru.olegcherednik.zip4jvm.io.in.file;

import org.apache.commons.io.IOUtils;
import ru.olegcherednik.zip4jvm.model.src.SrcFile;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 21.02.2019
 */
public class LittleEndianReadFile extends LittleEndianDataInputFile {

    private final Path file;

    public LittleEndianReadFile(Path file) throws IOException {
        super(file);
        this.file = file;
    }

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        return in.read(buf, offs, len);
    }

    @Override
    public SrcFile getSrcFile() {
        return SrcFile.of(file);
    }

    @Override
    public int getDisk() {
        return 0;
    }

    @Override
    public long getOffs() {
        try {
            return in.getFilePointer();
        } catch(IOException e) {
            return IOUtils.EOF;
        }
    }
}
