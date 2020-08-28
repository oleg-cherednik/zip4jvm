package ru.olegcherednik.zip4jvm.io.in.file;

import org.apache.commons.io.IOUtils;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;

import java.io.IOException;
import java.io.RandomAccessFile;

import static java.util.Objects.requireNonNull;

/**
 * @author Oleg Cherednik
 * @since 22.01.2020
 */
public class LittleEndianDataInputFile implements DataInputFile {

    private final SrcZip srcZip;
    private SrcZip.Disk disk;
    private RandomAccessFile in;

    public LittleEndianDataInputFile(SrcZip srcZip) throws IOException {
        this.srcZip = srcZip;
        openDisk(srcZip.getDisk(0));
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

            if (bytes == 0 || !openNextDisk())
                break;
        }

        return skipped;
    }

    @Override
    public void seek(long pos) throws IOException {
        openDisk(srcZip.getDiskByOffs(pos));
        in.seek(pos - disk.getOffs());
    }

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        int res = 0;
        int size = len;

        while (res < len) {
            int totalRead = in.read(buf, offs, size);

            if (totalRead > 0)
                res += totalRead;

            if (totalRead == IOUtils.EOF || totalRead < size) {
                openNextDisk();
                offs += Math.max(0, totalRead);
                size -= Math.max(0, totalRead);
            }
        }

        return res;
    }

    @Override
    public SrcZip getSrcZip() {
        return srcZip;
    }

    @Override
    public SrcZip.Disk getDisk() {
        return disk;
    }

    @Override
    public long getOffs() {
        return disk.getOffs() + getDiskRelativeOffs();
    }

    @Override
    public long getDiskRelativeOffs() {
        try {
            return in.getFilePointer();
        } catch(IOException e) {
            return IOUtils.EOF;
        }
    }

    private boolean openNextDisk() throws IOException {
        if (srcZip.isLast(disk))
            return false;

        // TODO disk.getPos() is 1 ahead (no need to +1)
        openDisk(requireNonNull(srcZip.getDisk(disk.getPos())));
        return true;
    }

    private void openDisk(SrcZip.Disk disk) throws IOException {
        if (this.disk == disk)
            return;

        close();
        in = new RandomAccessFile(disk.getFile().toFile(), "r");
        this.disk = disk;
    }

    @Override
    public void close() throws IOException {
        if (in != null)
            in.close();
    }

    @Override
    public String toString() {
        return "offs: " + getOffs() + " (0x" + Long.toHexString(getOffs()) + ')';
    }
}
