package ru.olegcherednik.zip4jvm.io.in;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.Objects;

/**
 * @author Oleg Cherednik
 * @since 22.01.2020
 */
public class SevenLittleEndianReadFile extends BaseDataInputFile {

    private final MultipleZip zip;
    private int disk;
    private long baseOffs;

    protected SevenLittleEndianReadFile(MultipleZip zip) throws IOException {
        super(zip.getDiskPath(0));
        this.zip = zip;
        disk = 0;
    }

    @Override
    public int skip(int bytes) throws IOException {
        int bytesSkipped = 0;

        for (int i = disk; bytesSkipped < bytes; ) {
            DiskInfo diskInfo = Objects.requireNonNull(zip.getDisk(i));
            boolean last = zip.getDisk(i + 1) == null;

            if (bytes - bytesSkipped < diskInfo.getSize() || last) {
                if (i != disk) {
                    openFile(diskInfo.getFile());
                    disk = diskInfo.getDisk();
                }

                bytesSkipped += in.skipBytes(bytes - bytesSkipped);
                break;
            } else
                bytesSkipped += diskInfo.getSize();
        }

        return bytesSkipped;
    }

    @Override
    public long length() throws IOException {
        return zip.length();
    }

    @Override
    public void seek(long pos) throws IOException {
        for (DiskInfo diskInfo : zip.getItems()) {
            if (diskInfo.getOffs() + diskInfo.getSize() < pos && diskInfo.getDisk() + 1 < zip.getTotalDisks())
                continue;

            if (disk != diskInfo.getDisk())
                openFile(diskInfo.getFile());

            in.seek(pos - diskInfo.getOffs());
            disk = diskInfo.getDisk();
            baseOffs = diskInfo.getOffs();
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
                openFile(zip.getDisk(++disk).getFile());
                offs += Math.max(0, total);
                size -= Math.max(0, total);
            }
        }

        return res;
    }

    @Override
    public int readSignature() throws IOException {
        // TODO probably it's better to use convert
        int b0 = in.read();
        int b1 = in.read();
        int b2 = in.read();
        int b3 = in.read();
        return b3 << 24 | b2 << 16 | b1 << 8 | b0;
    }

    @Override
    public long getBaseOffs() {
        try {
            return baseOffs + in.getFilePointer();
        } catch(IOException e) {
            return IOUtils.EOF;
        }
    }

}
