package ru.olegcherednik.zip4jvm.io.in.file;

import org.apache.commons.io.IOUtils;

import java.io.IOException;

import static java.util.Objects.requireNonNull;

/**
 * @author Oleg Cherednik
 * @since 22.01.2020
 */
public class LittleEndianSevenZipReadFile extends LittleEndianDataInputFile {

    private final SevenZipSplitSrcFile zip;
    private int itemPos;
    /** offs from the beginning of the first file */
    private long offs;

    protected LittleEndianSevenZipReadFile(SevenZipSplitSrcFile zip) throws IOException {
        super(zip.getItems().get(0).getFile());
        this.zip = zip;
        length = zip.getLength();
    }

    @Override
    public int skip(int bytes) throws IOException {
        int bytesSkipped = 0;

        for (int i = itemPos; bytesSkipped < bytes; ) {
            SrcFile.Item item = requireNonNull(zip.getDisk(i));
            boolean withinDisk = bytes - bytesSkipped < item.getLength();

            if (withinDisk || zip.isLast(item)) {
                if (i != this.itemPos) {
                    openFile(item.getFile());
                    this.itemPos = item.getPos();
                }

                bytesSkipped += in.skipBytes(bytes - bytesSkipped);
                break;
            }

            bytesSkipped += item.getLength();
        }

        return bytesSkipped;
    }

    @Override
    public void seek(long pos) throws IOException {
        for (SrcFile.Item item : zip.getItems()) {
            if (item.getOffs() + item.getLength() < pos && !zip.isLast(item))
                continue;

            if (itemPos != item.getPos())
                openFile(item.getFile());

            in.seek(pos - item.getOffs());
            itemPos = item.getPos();
            offs = item.getOffs();
            break;
        }
    }

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        int res = 0;
        int size = len;

        while (res < len) {
            int total = in.read(buf, offs, size);

            if (total > 0)
                res += total;

            if (total == IOUtils.EOF || total < size) {
                openFile(requireNonNull(zip.getDisk(++itemPos)).getFile());
                offs += Math.max(0, total);
                size -= Math.max(0, total);
            }
        }

        return res;
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
