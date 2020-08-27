package ru.olegcherednik.zip4jvm.io.in.file;

import org.apache.commons.io.IOUtils;

import java.io.IOException;

import static java.util.Objects.requireNonNull;

/**
 * @author Oleg Cherednik
 * @since 27.08.2020
 */
public class StandardSplitLittleEndianReadFile extends LittleEndianDataInputFile {

    private final SrcFile srcFile;
    private int itemPos;
    /** offs from the beginning of the first file */
    private long offs;

    public StandardSplitLittleEndianReadFile(SrcFile srcFile) throws IOException {
        super(srcFile.getItems().get(0).getFile());
        this.srcFile = srcFile;
        length = srcFile.getLength();
    }

    @Override
    public long skip(long bytes) throws IOException {
        long skipped = 0;

        while (bytes > 0) {
            long actual = super.skip(bytes);

            skipped += actual;
            bytes -= actual;

            if (bytes == 0)
                break;

            SrcFile.Item item = srcFile.getDisk(itemPos);

            if (srcFile.isLast(item))
                break;

            item = requireNonNull(srcFile.getDisk(itemPos + 1));
            openNextItem(item.getFile());
            offs = item.getOffs();
            itemPos = item.getPos();
        }

        return skipped;
    }

    @Override
    public void seek(long pos) throws IOException {
        for (SrcFile.Item item : srcFile.getItems()) {
            if (item.getOffs() + item.getLength() <= pos && !srcFile.isLast(item))
                continue;

            if (itemPos != item.getPos()) {
                openNextItem(item.getFile());
                offs = item.getOffs();
            }

            super.seek(pos - item.getOffs());
            itemPos = item.getPos();
            break;
        }
    }

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        int res = 0;
        int size = len;

        while (res < len) {
            int total = in.read(buf, offs, size);

            if (total > 0) {
                res += total;
            }

            if (total == IOUtils.EOF || total < size) {
                SrcFile.Item item = requireNonNull(srcFile.getDisk(++itemPos));
                openNextItem(item.getFile());
                this.offs = item.getOffs();
                offs += Math.max(0, total);
                size -= Math.max(0, total);
            }
        }

        return res;
    }

    @Override
    public SrcFile getSrcFile() {
        return srcFile;
    }

    @Override
    public int getDisk() {
        return srcFile.getItems().size() == 1 ? 0 : itemPos - 1;
    }

    @Override
    public long getOffs() {
        try {
            return offs + in.getFilePointer();
        } catch(IOException e) {
            return IOUtils.EOF;
        }
    }
}
