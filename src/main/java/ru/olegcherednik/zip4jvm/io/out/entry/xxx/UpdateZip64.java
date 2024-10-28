package ru.olegcherednik.zip4jvm.io.out.entry.xxx;

import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import static ru.olegcherednik.zip4jvm.model.ZipModel.MAX_ENTRY_SIZE;
import static ru.olegcherednik.zip4jvm.model.ZipModel.MAX_LOCAL_FILE_HEADER_OFFS;
import static ru.olegcherednik.zip4jvm.model.ZipModel.MAX_TOTAL_DISKS;

/**
 * @author Oleg Cherednik
 * @since 29.10.2024
 */
public final class UpdateZip64 {

    public void update(ZipEntry zipEntry) {
        if (zipEntry.getCompressedSize() > MAX_ENTRY_SIZE)
            zipEntry.setZip64(true);
        if (zipEntry.getUncompressedSize() > MAX_ENTRY_SIZE)
            zipEntry.setZip64(true);
        if (zipEntry.getDiskNo() > MAX_TOTAL_DISKS)
            zipEntry.setZip64(true);
        if (zipEntry.getLocalFileHeaderRelativeOffs() > MAX_LOCAL_FILE_HEADER_OFFS)
            zipEntry.setZip64(true);
    }

}
