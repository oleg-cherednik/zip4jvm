package ru.olegcherednik.zip4jvm.io.in.file;

import org.apache.commons.io.IOUtils;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;

import static java.util.Objects.requireNonNull;

/**
 * @author Oleg Cherednik
 * @since 22.01.2020
 */
public class LittleEndianDataInputFile implements DataInputFile {

    private final SrcZip srcZip;
    private SrcZip.Disk disk;

    private int itemPos;
    private long absOffs;

    private RandomAccessFile in;

    public LittleEndianDataInputFile(SrcZip srcZip) throws IOException {
        this.srcZip = srcZip;
        disk = srcZip.getDisks().get(0);
        openNextDisk(disk.getFile());
    }

    @Override
    public long length() {
        return srcZip.getLength();
    }

    @Override
    public long toLong(byte[] buf, int offs, int len) {
        long res = 0;

        for (int i = offs + len - 1; i >= offs; i--)
            res = res << 8 | buf[i] & 0xFF;

        return res;
    }

    @Override
    public long skip(long bytes) throws IOException {
        long skipped = 0;

        while (bytes > 0) {
            long actual = in.skipBytes((int)Math.min(Integer.MAX_VALUE, bytes));

            skipped += actual;
            bytes -= actual;

            if (bytes == 0)
                break;

            SrcZip.Disk disk = srcZip.getDisk(itemPos);

            if (srcZip.isLast(disk))
                break;

            disk = requireNonNull(srcZip.getDisk(itemPos + 1));
            openNextDisk(disk.getFile());
            this.disk = disk;
            absOffs = disk.getAbsOffs();
            itemPos = disk.getPos();
        }

        return skipped;
    }

    @Override
    public void seek(long pos) throws IOException {
        for (SrcZip.Disk disk : srcZip.getDisks()) {
            if (disk.getAbsOffs() + disk.getLength() <= pos && !srcZip.isLast(disk))
                continue;

            if (itemPos != disk.getPos()) {
                openNextDisk(disk.getFile());
                absOffs = disk.getAbsOffs();
            }

            in.seek(pos - disk.getAbsOffs());
            itemPos = disk.getPos();
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
                SrcZip.Disk disk = requireNonNull(srcZip.getDisk(++itemPos));
                openNextDisk(disk.getFile());
                this.disk = disk;
                absOffs = disk.getAbsOffs();
                offs += Math.max(0, total);
                size -= Math.max(0, total);
            }
        }

        return res;
    }

    @Override
    public SrcZip getSrcZip() {
        return srcZip;
    }

    @Override
    public int getDisk() {
        return srcZip.getDisks().size() == 1 ? 0 : itemPos - 1;
    }

    @Override
    public long getAbsOffs() {
        try {
            return absOffs + in.getFilePointer();
        } catch(IOException e) {
            return IOUtils.EOF;
        }
    }

    private void openNextDisk(Path file) throws IOException {
        if (srcZip.isLast(disk))
            return;

        close();
        in = new RandomAccessFile(file.toFile(), "r");
    }

    @Override
    public void close() throws IOException {
        if (in != null)
            in.close();
    }

    @Override
    public String toString() {
        return "offs: " + getAbsOffs() + " (0x" + Long.toHexString(getAbsOffs()) + ')';
    }
}
