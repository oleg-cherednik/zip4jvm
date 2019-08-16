package com.cop.zip4j.model;

import com.cop.zip4j.utils.BitUtils;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

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
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.cop.zip4j.utils.BitUtils.BIT0;
import static com.cop.zip4j.utils.BitUtils.BIT1;
import static com.cop.zip4j.utils.BitUtils.BIT2;
import static com.cop.zip4j.utils.BitUtils.BIT3;
import static com.cop.zip4j.utils.BitUtils.BIT4;
import static com.cop.zip4j.utils.BitUtils.BIT5;
import static com.cop.zip4j.utils.BitUtils.BIT6;
import static com.cop.zip4j.utils.BitUtils.BIT7;
import static java.nio.file.attribute.PosixFilePermission.GROUP_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.GROUP_READ;
import static java.nio.file.attribute.PosixFilePermission.GROUP_WRITE;
import static java.nio.file.attribute.PosixFilePermission.OTHERS_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.OTHERS_READ;
import static java.nio.file.attribute.PosixFilePermission.OTHERS_WRITE;
import static java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.OWNER_READ;
import static java.nio.file.attribute.PosixFilePermission.OWNER_WRITE;

/**
 * @author Oleg Cherednik
 * @since 16.08.2019
 */
public class ExternalFileAttributes implements Supplier<byte[]>, Consumer<Path> {

    private final OS os;

    // windows
    private boolean readOnly;
    private boolean hidden;
    private boolean system;
    private boolean archive;

    // posix
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

    public static ExternalFileAttributes readFromFile(Path path) {
        return new ExternalFileAttributes(path);
    }

    public static ExternalFileAttributes readFromData(byte[] data) {
        return new ExternalFileAttributes(data);
    }

    private ExternalFileAttributes(Path path) {
        os = OS.current();

        try {
            os.read(path, this);
        } catch(IOException ignored) {
        }
    }

    public ExternalFileAttributes(byte[] data) {
        os = OS.current();
        os.read(data, this);
    }

    @Override
    public byte[] get() {
        return os.apply(new byte[4], this);
    }

    @Override
    public void accept(Path path) {
        try {
            os.apply(path, this);
        } catch(IOException ignored) {
        }
    }

    @SuppressWarnings("NewClassNamingConvention")
    @RequiredArgsConstructor(access = AccessLevel.PACKAGE)
    private enum OS {
        UNKNOWN(os -> false),
        WINDOWS(os -> os.contains("win")) {
            @Override
            public void read(Path path, ExternalFileAttributes attr) throws IOException {
                DosFileAttributes dos = Files.getFileAttributeView(path, DosFileAttributeView.class).readAttributes();
                attr.readOnly = dos.isReadOnly();
                attr.hidden = dos.isHidden();
                attr.system = dos.isSystem();
                attr.archive = dos.isArchive();
            }

            @Override
            public void read(byte[] data, ExternalFileAttributes attr) {
                attr.readOnly = BitUtils.isBitSet(data[0], BIT0);
                attr.hidden = BitUtils.isBitSet(data[0], BIT1);
                attr.system = BitUtils.isBitSet(data[0], BIT2);
                attr.archive = BitUtils.isBitSet(data[0], BIT5);
            }

            @Override
            public byte[] apply(byte[] data, ExternalFileAttributes attr) {
                data[0] = BitUtils.updateBits(data[0], BIT0, attr.readOnly);
                data[0] = BitUtils.updateBits(data[0], BIT1, attr.hidden);
                data[0] = BitUtils.updateBits(data[0], BIT2, attr.system);
                data[0] = BitUtils.updateBits(data[0], BIT5, attr.archive);
                return data;
            }

            @Override
            public void apply(@NonNull Path path, ExternalFileAttributes attr) throws IOException {
                DosFileAttributeView dos = Files.getFileAttributeView(path, DosFileAttributeView.class);
                dos.setReadOnly(attr.readOnly);
                dos.setHidden(attr.hidden);
                dos.setSystem(attr.system);
                dos.setArchive(attr.archive);
            }
        },
        POSIX(os -> os.contains("mac") || os.contains("nux")) {
            @Override
            public void read(Path path, ExternalFileAttributes attr) throws IOException {
                Set<PosixFilePermission> permissions = Files.getFileAttributeView(path, PosixFileAttributeView.class).readAttributes().permissions();

                attr.othersExecute = permissions.contains(OTHERS_EXECUTE);
                attr.othersWrite = permissions.contains(OTHERS_WRITE);
                attr.othersRead = permissions.contains(OTHERS_READ);
                attr.groupExecute = permissions.contains(GROUP_EXECUTE);
                attr.groupWrite = permissions.contains(GROUP_WRITE);
                attr.groupRead = permissions.contains(GROUP_READ);
                attr.ownerExecute = permissions.contains(OWNER_EXECUTE);
                attr.ownerWrite = permissions.contains(OWNER_WRITE);
                attr.ownerRead = permissions.contains(OWNER_READ);
                attr.directory = Files.isDirectory(path);
                attr.regularFile = Files.isRegularFile(path);
            }

            @Override
            public void read(byte[] data, ExternalFileAttributes attr) {
                attr.othersExecute = BitUtils.isBitSet(data[2], BIT0);
                attr.othersWrite = BitUtils.isBitSet(data[2], BIT1);
                attr.othersRead = BitUtils.isBitSet(data[2], BIT2);
                attr.groupExecute = BitUtils.isBitSet(data[2], BIT3);
                attr.groupWrite = BitUtils.isBitSet(data[2], BIT4);
                attr.groupRead = BitUtils.isBitSet(data[2], BIT5);
                attr.ownerExecute = BitUtils.isBitSet(data[2], BIT6);
                attr.ownerWrite = BitUtils.isBitSet(data[2], BIT7);
                attr.ownerRead = BitUtils.isBitSet(data[3], BIT0);
                attr.directory = BitUtils.isBitSet(data[3], BIT6);
                attr.regularFile = BitUtils.isBitSet(data[3], BIT7);
            }

            @Override
            public byte[] apply(byte[] data, ExternalFileAttributes attr) {
                data[2] = BitUtils.updateBits(data[0], BIT0, attr.othersExecute);
                data[2] = BitUtils.updateBits(data[0], BIT1, attr.othersWrite);
                data[2] = BitUtils.updateBits(data[0], BIT2, attr.othersRead);
                data[2] = BitUtils.updateBits(data[0], BIT3, attr.groupExecute);
                data[2] = BitUtils.updateBits(data[0], BIT4, attr.groupWrite);
                data[2] = BitUtils.updateBits(data[0], BIT4, attr.groupRead);
                data[2] = BitUtils.updateBits(data[0], BIT5, attr.ownerExecute);
                data[2] = BitUtils.updateBits(data[0], BIT7, attr.ownerWrite);
                data[3] = BitUtils.updateBits(data[0], BIT0, attr.ownerRead);
                data[3] = BitUtils.updateBits(data[0], BIT6, attr.directory);
                data[3] = BitUtils.updateBits(data[0], BIT7, attr.regularFile);
                return data;
            }

            @Override
            public void apply(@NonNull Path path, ExternalFileAttributes attr) throws IOException {
                Set<PosixFilePermission> permissions = EnumSet.noneOf(PosixFilePermission.class);
                add(attr.othersExecute, permissions, OTHERS_EXECUTE);
                add(attr.othersWrite, permissions, OTHERS_WRITE);
                add(attr.othersRead, permissions, OTHERS_READ);
                add(attr.groupExecute, permissions, GROUP_EXECUTE);
                add(attr.groupWrite, permissions, GROUP_WRITE);
                add(attr.groupRead, permissions, GROUP_READ);
                add(attr.ownerExecute, permissions, OWNER_EXECUTE);
                add(attr.ownerWrite, permissions, OWNER_WRITE);
                add(attr.ownerRead, permissions, OWNER_READ);

                if (!permissions.isEmpty())
                    Files.setPosixFilePermissions(path, permissions);
            }

            private void add(boolean exists, Set<PosixFilePermission> permissions, PosixFilePermission permission) {
                if (exists)
                    permissions.add(permission);
            }
        };

        private final Predicate<String> is;

        public void read(Path path, ExternalFileAttributes attr) throws IOException {
        }

        public void read(byte[] data, ExternalFileAttributes attr) {
        }

        public byte[] apply(byte[] data, ExternalFileAttributes attr) {
            return data;
        }

        public void apply(@NonNull Path path, ExternalFileAttributes attr) throws IOException {
        }

        public static OS current() {
            String os = System.getProperty("os.name").toLowerCase();

            for (OS system : values())
                if (system.is.test(os))
                    return system;

            return UNKNOWN;
        }

    }


}
