package ru.olegcherednik.zip4jvm.io.in.file;

import org.apache.commons.io.IOUtils;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;

import java.io.IOException;

import static java.util.Objects.requireNonNull;

/**
 * @author Oleg Cherednik
 * @since 27.08.2020
 */
public class SrcZipLittleEndianReadFile extends LittleEndianDataInputFile {

    private final SrcZip srcZip;
    private int itemPos;
    private long absOffs;

    public SrcZipLittleEndianReadFile(SrcZip srcZip) throws IOException {
        super(srcZip.getDisks().get(0).getFile());
        this.srcZip = srcZip;
        length = srcZip.getLength();
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

            SrcZip.Disk disk = srcZip.getDisk(itemPos);

            if (srcZip.isLast(disk))
                break;

            disk = requireNonNull(srcZip.getDisk(itemPos + 1));
            openNextItem(disk.getFile());
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
                openNextItem(disk.getFile());
                absOffs = disk.getAbsOffs();
            }

            super.seek(pos - disk.getAbsOffs());
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
                openNextItem(disk.getFile());
                this.absOffs = disk.getAbsOffs();
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
}
