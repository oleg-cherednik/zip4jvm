package ru.olegcherednik.zip4jvm.io.readers.block;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.io.in.SingleZipInputStream;
import ru.olegcherednik.zip4jvm.io.in.SplitZipInputStream;
import ru.olegcherednik.zip4jvm.io.readers.block.aes.AesEncryptionHeader;
import ru.olegcherednik.zip4jvm.io.readers.block.aes.BlockAesHeaderReader;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.model.LocalFileHeader;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.block.BlockZipEntryModel;
import ru.olegcherednik.zip4jvm.model.block.Diagnostic;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 20.10.2019
 */
@RequiredArgsConstructor
public class BlockZipEntryModelReader {

    private final ZipModel zipModel;
    private final Function<Charset, Charset> charsetCustomizer;
    private final Diagnostic.ZipEntryBlock zipEntryBlock;

    public BlockZipEntryModel read() throws IOException {
        Map<String, LocalFileHeader> localFileHeaders = new LinkedHashMap<>();

        for (ZipEntry zipEntry : zipModel.getEntries()) {
            try (DataInput in = createDataInput(zipModel, zipEntry)) {
                zipEntryBlock.addLocalFileHeader();
                zipEntryBlock.getLocalFileHeader().setDisk(zipEntry.getDisk());
                long offs = zipEntry.getLocalFileHeaderOffs();
                LocalFileHeader localFileHeader = new BlockLocalFileHeaderReader(offs, charsetCustomizer, zipEntryBlock.getLocalFileHeader()).read(
                        in);
                zipEntryBlock.saveLocalFileHeader(localFileHeader.getFileName());
                localFileHeaders.put(localFileHeader.getFileName(), localFileHeader);

                //---

                Encryption encryption = zipEntry.getEncryption();
                Encryption.CreateDecoder createDecoder = encryption.getCreateDecoder();

                if (zipEntry.getEncryption() == Encryption.AES_256) {
                    BlockAesHeaderReader reader = new BlockAesHeaderReader(zipEntry.getStrength(), zipEntry.getCompressedSize());
                    AesEncryptionHeader encryptionHeader = reader.read(in);
                    zipEntryBlock.saveEncryptionHeader(zipEntry.getFileName(), encryptionHeader);
                }


                int a = 0;
                a++;
            }
        }

        return new BlockZipEntryModel(zipEntryBlock, localFileHeaders);
    }

    private static DataInput createDataInput(ZipModel zipModel, ZipEntry zipEntry) throws IOException {
        if (zipModel.isSplit())
            return new SplitZipInputStream(zipModel, zipEntry.getDisk());
        return new SingleZipInputStream(zipModel.getFile());
    }
}
