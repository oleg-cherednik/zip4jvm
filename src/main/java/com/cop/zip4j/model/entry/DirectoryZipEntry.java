package com.cop.zip4j.model.entry;

import com.cop.zip4j.model.Compression;
import com.cop.zip4j.model.Encryption;
import com.cop.zip4j.utils.ZipUtils;
import lombok.NonNull;
import org.apache.commons.lang.StringUtils;

import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 26.07.2019
 */
public class DirectoryZipEntry extends PathZipEntry {

    public DirectoryZipEntry(Path dir, int lastModifiedTime) {
        super(dir, lastModifiedTime);
    }

    @Override
    public void setCompression(@NonNull Compression compression) {
        this.compression = Compression.STORE;
    }

    @Override
    public void setEncryption(@NonNull Encryption encryption) {
        this.encryption = Encryption.OFF;
    }

    @Override
    public long getCompressedSize() {
        return 0;
    }

    @Override
    public void setName(String name) {
        if(StringUtils.isNotBlank(name) && !ZipUtils.isDirectory(name))
            name += '/';
        super.setName(name);
    }

}
