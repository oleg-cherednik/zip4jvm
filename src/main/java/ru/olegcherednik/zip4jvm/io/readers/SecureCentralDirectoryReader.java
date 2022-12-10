package ru.olegcherednik.zip4jvm.io.readers;

import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeaderDecoder;
import ru.olegcherednik.zip4jvm.exception.IncorrectPasswordException;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.in.data.LittleEndianDataInput;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.password.PasswordProvider;
import ru.olegcherednik.zip4jvm.model.Zip64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 10.10.2019
 */
public final class SecureCentralDirectoryReader extends CentralDirectoryReader {

    private final Zip64.ExtensibleDataSector extensibleDataSector;
    private final PasswordProvider passwordProvider;

    public SecureCentralDirectoryReader(long totalEntries,
                                        Function<Charset, Charset> customizeCharset,
                                        Zip64.ExtensibleDataSector extensibleDataSector,
                                        PasswordProvider passwordProvider) {
        super(totalEntries, customizeCharset);
        this.extensibleDataSector = Objects.requireNonNull(extensibleDataSector);
        this.passwordProvider = passwordProvider;
    }

    @Override
    public CentralDirectory read(DataInput in) throws IOException {
        try {
            char[] password = passwordProvider.getCentralDirectoryPassword();
            Cipher cipher = new DecryptionHeaderDecoder(password).readAndCreateCipher(in);
            byte[] buf = cipher.update(in.readBytes((int)extensibleDataSector.getCompressedSize()));
            CentralDirectoryReader centralDirectoryReader = new CentralDirectoryReader(totalEntries, customizeCharset);
            return centralDirectoryReader.read(new LittleEndianDataInput(buf));
        } catch(IncorrectPasswordException | BadPaddingException e) {
            throw new IncorrectPasswordException("Central Directory");
        } catch(Zip4jvmException | IOException e) {
            throw e;
        } catch(Exception e) {
            throw new Zip4jvmException(e);
        }
    }
}
