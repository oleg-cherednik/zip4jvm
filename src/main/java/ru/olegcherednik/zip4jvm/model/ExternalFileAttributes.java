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
import java.util.Optional;
import java.util.Set;
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
public abstract class ExternalFileAttributes implements Supplier<byte[]> {

    public static final Supplier<String> PROP_OS_NAME = () -> System.getProperty("os.name");

    public static final String WIN = "win";
    public static final String MAC = "mac";
    public static final String UNIX = "nux";

    @SuppressWarnings("StaticInitializerReferencesSubClass")
    public static final ExternalFileAttributes NULL = new Unknown();
    public static final int SIZE = 4;

    public static ExternalFileAttributes build(Supplier<String> osNameProvider) {
        String os = Optional.ofNullable(osNameProvider).orElse(() -> "<unknown>").get().toLowerCase();

        if (os.contains(WIN))
            return new Windows();
        if (os.contains(MAC) || os.contains(UNIX))
            return new Posix();

        return NULL;
    }

    public abstract ExternalFileAttributes readFrom(Path path) throws IOException;

    public abstract ExternalFileAttributes readFrom(byte[] data);

    public abstract void apply(Path path) throws IOException;

    private static class Unknown extends ExternalFileAttributes {

        @Override
        public Unknown readFrom(Path path) throws IOException {
            return this;
        }

        @Override
        public Unknown readFrom(byte[] data) {
            return this;
        }

        @Override
        public void apply(Path path) throws IOException {
            /* nothing to apply */
        }

        @Override
        public byte[] get() {
            return new byte[SIZE];
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
        private final byte[] data = new byte[SIZE];

        public static boolean isValid(byte[] data) {
            return data[0] != 0;
        }

        private Windows() {
            defaults();
        }

        private void defaults() {
            readOnly = false;
            hidden = false;
            system = false;
            archive = true;
        }

        @Override
        public Windows readFrom(Path path) throws IOException {
            defaults();
            DosFileAttributes dos = Files.getFileAttributeView(path, DosFileAttributeView.class).readAttributes();
            readOnly = dos.isReadOnly();
            hidden = dos.isHidden();
            system = dos.isSystem();
            archive = dos.isArchive();
            return this;
        }

        @Override
        public Windows readFrom(byte[] data) {
            System.arraycopy(data, 0, this.data, 0, SIZE);

            defaults();

            if (isValid(data)) {
                readOnly = isReadOnly(data);
                hidden = BitUtils.isBitSet(data[0], BIT1);
                system = BitUtils.isBitSet(data[0], BIT2);
                archive = BitUtils.isBitSet(data[0], BIT5);
            } else if (Posix.isValid(data))
                readOnly = Posix.isReadOnly(data);

            return this;
        }

        @Override
        public void apply(Path path) throws IOException {
            DosFileAttributeView dos = Files.getFileAttributeView(path, DosFileAttributeView.class);
            dos.setReadOnly(readOnly);
            dos.setHidden(hidden);
            dos.setSystem(system);
            dos.setArchive(archive);
        }

        @Override
        public byte[] get() {
            byte[] data = new byte[SIZE];

            data[0] = BitUtils.updateBits(data[0], BIT0, readOnly);
            data[0] = BitUtils.updateBits(data[0], BIT1, hidden);
            data[0] = BitUtils.updateBits(data[0], BIT2, system);
            data[0] = BitUtils.updateBits(data[0], BIT5, archive);

            data[1] = this.data[1];
            data[2] = this.data[2];
            data[3] = this.data[3];
            return data;
        }

        @Override
        public String toString() {
            return "win";
        }

        private static boolean isReadOnly(byte[] data) {
            return BitUtils.isBitSet(data[0], BIT0);
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
        private final byte[] data = new byte[SIZE];

        public static boolean isValid(byte[] data) {
            return data[2] != 0 || data[3] != 0;
        }

        private Posix() {
            defaults();
        }

        private void defaults() {
            othersExecute = false;
            othersWrite = false;
            othersRead = true;
            groupExecute = false;
            groupWrite = false;
            groupRead = true;
            ownerExecute = false;
            ownerWrite = true;
            ownerRead = true;
            directory = false;
            regularFile = true;
        }

        @Override
        public Posix readFrom(Path path) throws IOException {
            defaults();
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

            return this;
        }

        @Override
        public Posix readFrom(byte[] data) {
            System.arraycopy(data, 0, this.data, 0, SIZE);
            defaults();

            if (isValid(data)) {
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
            } else if (Windows.isValid(data))
                ownerWrite = !Windows.isReadOnly(data);

            return this;
        }

        @Override
        public void apply(Path path) throws IOException {
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
            Files.setPosixFilePermissions(path, permissions);
        }

        @Override
        public byte[] get() {
            byte[] data = new byte[SIZE];
            data[0] = this.data[0];
            data[1] = this.data[1];

            data[2] = BitUtils.updateBits(data[2], BIT0, othersExecute);
            data[2] = BitUtils.updateBits(data[2], BIT1, othersWrite);
            data[2] = BitUtils.updateBits(data[2], BIT2, othersRead);
            data[2] = BitUtils.updateBits(data[2], BIT3, groupExecute);
            data[2] = BitUtils.updateBits(data[2], BIT4, groupWrite);
            data[2] = BitUtils.updateBits(data[2], BIT5, groupRead);
            data[2] = BitUtils.updateBits(data[2], BIT6, ownerExecute);
            data[2] = BitUtils.updateBits(data[2], BIT7, ownerWrite);
            data[3] = BitUtils.updateBits(data[3], BIT0, ownerRead);
            data[3] = BitUtils.updateBits(data[3], BIT6, directory);
            data[3] = BitUtils.updateBits(data[3], BIT7, regularFile);
            return data;
        }

        @Override
        public String toString() {
            return "posix";
        }

        private static void addIfSet(boolean exists, Set<PosixFilePermission> permissions, PosixFilePermission permission) {
            if (exists)
                permissions.add(permission);
        }

        private static boolean isReadOnly(byte[] data) {
            return BitUtils.isBitClear(data[2], BIT1 | BIT4 | BIT7);
        }
    }

}
