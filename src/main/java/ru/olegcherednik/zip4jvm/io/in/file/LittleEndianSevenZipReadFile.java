package ru.olegcherednik.zip4jvm.io.in.file;

import org.apache.commons.io.IOUtils;

import java.io.IOException;

import static java.util.Objects.requireNonNull;

/**
 * @author Oleg Cherednik
 * @since 22.01.2020
 */
public class LittleEndianSevenZipReadFile extends LittleEndianDataInputFile {

    private final SevenZipSplitZip zip;
    private int disk;
    /** offs from the beginning of the first file */
    private long offs;

    protected LittleEndianSevenZipReadFile(SevenZipSplitZip zip) throws IOException {
        super(zip.getDiskFile(0));
        this.zip = zip;
        length = zip.getLength();
    }

    @Override
    public int skip(int bytes) throws IOException {
        int bytesSkipped = 0;

        for (int i = disk; bytesSkipped < bytes; ) {
            Disk disk = requireNonNull(zip.getDisk(i));
            boolean withinDisk = bytes - bytesSkipped < disk.getLength();

            if (withinDisk || zip.isLast(disk)) {
                if (i != this.disk) {
                    openFile(disk.getFile());
                    this.disk = disk.getNum();
                }

                bytesSkipped += in.skipBytes(bytes - bytesSkipped);
                break;
            }

            bytesSkipped += disk.getLength();
        }

        return bytesSkipped;
    }

    @Override
    public void seek(long pos) throws IOException {
        for (Disk disk : zip.getItems()) {
            if (disk.getOffs() + disk.getLength() < pos && !zip.isLast(disk))
                continue;

            if (this.disk != disk.getNum())
                openFile(disk.getFile());

            in.seek(pos - disk.getOffs());
            this.disk = disk.getNum();
            offs = disk.getOffs();
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
                openFile(requireNonNull(zip.getDisk(++disk)).getFile());
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
