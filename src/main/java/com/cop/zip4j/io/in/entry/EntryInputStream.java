package com.cop.zip4j.io.in.entry;

import com.cop.zip4j.core.readers.LocalFileHeaderReader;
import com.cop.zip4j.crypto.Decoder;
import com.cop.zip4j.exception.Zip4jException;
import com.cop.zip4j.io.InflaterInputStream;
import com.cop.zip4j.io.PartInputStream;
import com.cop.zip4j.io.ZipInputStream;
import com.cop.zip4j.io.in.DataInput;
import com.cop.zip4j.model.CentralDirectory;
import com.cop.zip4j.model.Compression;
import com.cop.zip4j.model.LocalFileHeader;
import com.cop.zip4j.model.ZipModel;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Oleg Cherednik
 * @since 04.08.2019
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class EntryInputStream extends InputStream {

    public static InputStream create(@NonNull CentralDirectory.FileHeader fileHeader, char[] password, DataInput in, ZipModel zipModel)
            throws IOException {
        long pos = in.getOffs();
        LocalFileHeader localFileHeader = new LocalFileHeaderReader(fileHeader).read(in);
        in.seek(pos);
        Decoder decoder = localFileHeader.getEncryption().decoder(in, localFileHeader, password);

        long comprSize = decoder.getCompressedSize(localFileHeader);

        in.seek(decoder.getOffs(localFileHeader));

        Compression compression = fileHeader.getCompression();

        if (compression == Compression.STORE)
            return new ZipInputStream(new PartInputStream(in, comprSize, decoder, zipModel), fileHeader, decoder);
        if (compression == Compression.DEFLATE)
            return new ZipInputStream(new InflaterInputStream(in, comprSize, decoder, zipModel, fileHeader), fileHeader, decoder);

        throw new Zip4jException("Compression is not supported: " + compression);
    }

}
