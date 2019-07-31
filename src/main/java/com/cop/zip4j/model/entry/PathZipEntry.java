package com.cop.zip4j.model.entry;

import com.cop.zip4j.model.aes.AesStrength;
import com.cop.zip4j.model.CompressionLevel;
import com.cop.zip4j.model.CompressionMethod;
import com.cop.zip4j.model.Encryption;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 26.07.2019
 */
@Getter
@RequiredArgsConstructor
public abstract class PathZipEntry extends ZipEntry {

    protected final Path path;
    @Setter
    protected String name;

    protected CompressionMethod compressionMethod = CompressionMethod.STORE;
    protected CompressionLevel compressionLevel = CompressionLevel.NORMAL;
    protected Encryption encryption = Encryption.OFF;

    @Setter
    protected AesStrength strength = AesStrength.NONE;
    @Setter
    protected char[] password;

    @Override
    public boolean isRegularFile() {
        return Files.isRegularFile(path);
    }

    @Override
    public boolean isDirectory() {
        return Files.isDirectory(path);
    }

    @Override
    public String getAbsolutePath() {
        return path.toAbsolutePath().toString();
    }

    public boolean isRoot() {
        return "/".equals(name) || "\\".equals(name);
    }

    public abstract void setCompressionMethod(@NonNull CompressionMethod compressionMethod) throws IOException;

//    public CompressionMethod getCompressionMethod() {
//        return encryption == Encryption.AES ? CompressionMethod.AES_ENC : compressionMethod;
//    }

    public void setCompressionLevel(@NonNull CompressionLevel compressionLevel) throws IOException {
        this.compressionLevel = CompressionLevel.NORMAL;
    }

    public abstract void setEncryption(@NonNull Encryption encryption);

    public AesStrength getStrength() {
        return encryption == Encryption.AES || encryption == Encryption.AES_NEW ? strength : AesStrength.NONE;
    }

}
