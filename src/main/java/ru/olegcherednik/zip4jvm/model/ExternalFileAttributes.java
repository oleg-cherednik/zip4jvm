package ru.olegcherednik.zip4jvm.model;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.olegcherednik.zip4jvm.utils.BitUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.DosFileAttributes;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.nio.file.attribute.PosixFilePermission.GROUP_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.GROUP_READ;
import static java.nio.file.attribute.PosixFilePermission.GROUP_WRITE;
import static java.nio.file.attribute.PosixFilePermission.OTHERS_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.OTHERS_READ;
import static java.nio.file.attribute.PosixFilePermission.OTHERS_WRITE;
import static java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.OWNER_READ;
import static java.nio.file.attribute.PosixFilePermission.OWNER_WRITE;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT0;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT1;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT2;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT3;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT4;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT5;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT6;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT7;

/**
 * @author Oleg Cherednik
 * @since 16.08.2019
 */
@SuppressWarnings("MethodCanBeVariableArityMethod")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class ExternalFileAttributes implements Supplier<byte[]>, Consumer<Path> {

    public static final String WIN = "win";
    public static final String MAC = "mac";
    public static final String UNIX = "nux";

    public static final ExternalFileAttributes NULL = new Unknown();
    public static final int SIZE = 4;

    public static ExternalFileAttributes createOperationBasedDelegate(Path path, Supplier<String> osNameProvider) throws IOException {
        String os = osNameProvider.get().toLowerCase();
        ExternalFileAttributes attributes = NULL;

        if (os.contains(WIN))
            attributes = new Windows();
        else if (os.contains(MAC) || os.contains(UNIX))
            attributes = new Posix();

        attributes.readFrom(path);
        return attributes;
    }

    public static ExternalFileAttributes createDataBasedDelegate(byte[] data) {
        ExternalFileAttributes attributes = NULL;

        if (Windows.isValid(data))
            attributes = new Windows();
        else if (Posix.isValid(data))
            attributes = new Posix();

        attributes.readFrom(data);
        return attributes;
    }

    @Override
    public byte[] get() {
        byte[] data = new byte[SIZE];
        saveToRowData(data);
        return data;
    }

    @Override
    public void accept(Path path) {
        try {
            applyToPath(path);
        } catch(IOException ignored) {
        }
    }

    public abstract void readFrom(Path path) throws IOException;

    public abstract void readFrom(byte[] data);

    protected abstract void saveToRowData(byte[] data);

    protected abstract void applyToPath(Path path) throws IOException;

    private static class Unknown extends ExternalFileAttributes {

        @Override
        public void readFrom(Path path) throws IOException {
            /* nothing to read */
        }

        @Override
        public void readFrom(byte[] data) {
            /* nothing to read */
        }

        @Override
        protected void saveToRowData(byte[] data) {
            /* nothing to save */
        }

        @Override
        protected void applyToPath(Path path) throws IOException {
            /* nothing to apply */
        }

        @Override
        public String toString() {
            return "<null>";
        }

    }

    private static class Windows extends ExternalFileAttributes {

        private boolean readOnly;
        private boolean hidden;
        private boolean system;
        private boolean archive;

        public static boolean isValid(byte[] data) {
            return data[0] != 0;
        }

        @Override
        public void readFrom(Path path) throws IOException {
            DosFileAttributes dos = Files.getFileAttributeView(path, DosFileAttributeView.class).readAttributes();
            readOnly = dos.isReadOnly();
            hidden = dos.isHidden();
            system = dos.isSystem();
            archive = dos.isArchive();
        }

        @Override
        public void readFrom(byte[] data) {
            readOnly = BitUtils.isBitSet(data[0], BIT0);
            hidden = BitUtils.isBitSet(data[0], BIT1);
            system = BitUtils.isBitSet(data[0], BIT2);
            archive = BitUtils.isBitSet(data[0], BIT5);
        }

        @Override
        protected void saveToRowData(byte[] data) {
            data[0] = BitUtils.updateBits(data[0], BIT0, readOnly);
            data[0] = BitUtils.updateBits(data[0], BIT1, hidden);
            data[0] = BitUtils.updateBits(data[0], BIT2, system);
            data[0] = BitUtils.updateBits(data[0], BIT5, archive);
        }

        @Override
        protected void applyToPath(Path path) throws IOException {
            //
        }

        @Override
        public String toString() {
            return "win";
        }
    }

    private static class Posix extends ExternalFileAttributes {

        private boolean othersExecute;
        private boolean othersWrite;
        private boolean othersRead;
        private boolean groupExecute;
        private boolean groupWrite;
        private boolean groupRead;
        private boolean ownerExecute;
        private boolean ownerWrite;
        private boolean ownerRead;
        private boolean directory;
        private boolean regularFile;

        public static boolean isValid(byte[] data) {
            return data[0] != 0 || data[2] != 0 && data[3] != 0;
        }

        @Override
        public void readFrom(Path path) throws IOException {
            Set<PosixFilePermission> permissions = Files.getFileAttributeView(path, PosixFileAttributeView.class).readAttributes().permissions();

            othersExecute = permissions.contains(OTHERS_EXECUTE);
            othersWrite = permissions.contains(OTHERS_WRITE);
            othersRead = permissions.contains(OTHERS_READ);
            groupExecute = permissions.contains(GROUP_EXECUTE);
            groupWrite = permissions.contains(GROUP_WRITE);
            groupRead = permissions.contains(GROUP_READ);
            ownerExecute = permissions.contains(OWNER_EXECUTE);
            ownerWrite = permissions.contains(OWNER_WRITE);
            ownerRead = permissions.contains(OWNER_READ);
            directory = Files.isDirectory(path);
            regularFile = Files.isRegularFile(path);
        }

        @Override
        public void readFrom(byte[] data) {
            othersExecute = BitUtils.isBitSet(data[2], BIT0);
            othersWrite = BitUtils.isBitSet(data[2], BIT1);
            othersRead = BitUtils.isBitSet(data[2], BIT2);
            groupExecute = BitUtils.isBitSet(data[2], BIT3);
            groupWrite = BitUtils.isBitSet(data[2], BIT4);
            groupRead = BitUtils.isBitSet(data[2], BIT5);
            ownerExecute = BitUtils.isBitSet(data[2], BIT6);
            ownerWrite = BitUtils.isBitSet(data[2], BIT7);
            ownerRead = BitUtils.isBitSet(data[3], BIT0);
            directory = BitUtils.isBitSet(data[3], BIT6);
            regularFile = BitUtils.isBitSet(data[3], BIT7);
        }

        @Override
        protected void saveToRowData(byte[] data) {
            data[2] = BitUtils.updateBits(data[0], BIT0, othersExecute);
            data[2] = BitUtils.updateBits(data[0], BIT1, othersWrite);
            data[2] = BitUtils.updateBits(data[0], BIT2, othersRead);
            data[2] = BitUtils.updateBits(data[0], BIT3, groupExecute);
            data[2] = BitUtils.updateBits(data[0], BIT4, groupWrite);
            data[2] = BitUtils.updateBits(data[0], BIT4, groupRead);
            data[2] = BitUtils.updateBits(data[0], BIT5, ownerExecute);
            data[2] = BitUtils.updateBits(data[0], BIT7, ownerWrite);
            data[3] = BitUtils.updateBits(data[0], BIT0, ownerRead);
            data[3] = BitUtils.updateBits(data[0], BIT6, directory);
            data[3] = BitUtils.updateBits(data[0], BIT7, regularFile);
        }

        @Override
        protected void applyToPath(Path path) throws IOException {
            Set<PosixFilePermission> permissions = EnumSet.noneOf(PosixFilePermission.class);
            addIfSet(othersExecute, permissions, OTHERS_EXECUTE);
            addIfSet(othersWrite, permissions, OTHERS_WRITE);
            addIfSet(othersRead, permissions, OTHERS_READ);
            addIfSet(groupExecute, permissions, GROUP_EXECUTE);
            addIfSet(groupWrite, permissions, GROUP_WRITE);
            addIfSet(groupRead, permissions, GROUP_READ);
            addIfSet(ownerExecute, permissions, OWNER_EXECUTE);
            addIfSet(ownerWrite, permissions, OWNER_WRITE);
            addIfSet(ownerRead, permissions, OWNER_READ);

            if (!permissions.isEmpty())
                Files.setPosixFilePermissions(path, permissions);
        }

        private static void addIfSet(boolean exists, Set<PosixFilePermission> permissions, PosixFilePermission permission) {
            if (exists)
                permissions.add(permission);
        }

        @Override
        public String toString() {
            return "posix";
        }
    }

}
