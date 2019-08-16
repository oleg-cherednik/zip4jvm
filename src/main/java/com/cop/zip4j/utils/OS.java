package com.cop.zip4j.utils;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.ArrayUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.DosFileAttributes;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Predicate;

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
@SuppressWarnings({ "NewClassNamingConvention", "MethodCanBeVariableArityMethod" })
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum OS {
    UNKNOWN(os -> false),
    WINDOWS(os -> os.contains("win")) {
        @Override
        public byte[] getAttributes(@NonNull Path path) throws IOException {
            DosFileAttributes dos = Files.getFileAttributeView(path, DosFileAttributeView.class).readAttributes();

            int zero = 0;
            zero = BitUtils.updateBits(zero, BIT0, dos.isReadOnly());
            zero = BitUtils.updateBits(zero, BIT1, dos.isHidden());
            zero = BitUtils.updateBits(zero, BIT2, dos.isSystem());
            zero = BitUtils.updateBits(zero, BIT5, dos.isArchive());
            return new byte[] { (byte)zero, 0, 0, 0 };
        }

        @Override
        public void applyAttribute(@NonNull Path path, byte[] attr) throws IOException {
            int zero = ArrayUtils.getLength(attr) < 1 ? 0 : attr[0];

            if (zero != 0) {
                DosFileAttributeView dos = Files.getFileAttributeView(path, DosFileAttributeView.class);
                dos.setReadOnly(BitUtils.isBitSet(zero, BIT0));
                dos.setHidden(BitUtils.isBitSet(zero, BIT1));
                dos.setSystem(BitUtils.isBitSet(zero, BIT2));
                dos.setArchive(BitUtils.isBitSet(zero, BIT5));
            }
        }
    },
    POSIX(os -> os.contains("mac") || os.contains("nux")) {
        @Override
        public byte[] getAttributes(@NonNull Path path) throws IOException {
            Set<PosixFilePermission> permissions = Files.getFileAttributeView(path, PosixFileAttributeView.class).readAttributes().permissions();

            int two = 0;
            two = BitUtils.updateBits(two, BIT0, permissions.contains(OTHERS_EXECUTE));
            two = BitUtils.updateBits(two, BIT1, permissions.contains(OTHERS_WRITE));
            two = BitUtils.updateBits(two, BIT2, permissions.contains(OTHERS_READ));
            two = BitUtils.updateBits(two, BIT3, permissions.contains(GROUP_EXECUTE));
            two = BitUtils.updateBits(two, BIT4, permissions.contains(GROUP_WRITE));
            two = BitUtils.updateBits(two, BIT5, permissions.contains(GROUP_READ));
            two = BitUtils.updateBits(two, BIT6, permissions.contains(OWNER_EXECUTE));
            two = BitUtils.updateBits(two, BIT7, permissions.contains(OWNER_WRITE));

            int three = 0;
            three = BitUtils.updateBits(three, BIT0, permissions.contains(OWNER_READ));
            three = BitUtils.updateBits(three, BIT6, Files.isDirectory(path));
            three = BitUtils.updateBits(three, BIT7, Files.isRegularFile(path));

            return new byte[] { 0, 0, (byte)two, (byte)three };
        }

        @Override
        public void applyAttribute(@NonNull Path path, byte[] attr) throws IOException {
            int two = ArrayUtils.getLength(attr) < 3 ? 0 : attr[2];
            int three = ArrayUtils.getLength(attr) < 4 ? 0 : attr[3];

            Set<PosixFilePermission> permissions = EnumSet.noneOf(PosixFilePermission.class);

            if (two != 0) {
                addIfBitSet(two, BIT0, permissions, OTHERS_EXECUTE);
                addIfBitSet(two, BIT1, permissions, OTHERS_WRITE);
                addIfBitSet(two, BIT2, permissions, OTHERS_READ);
                addIfBitSet(two, BIT3, permissions, GROUP_EXECUTE);
                addIfBitSet(two, BIT4, permissions, GROUP_WRITE);
                addIfBitSet(two, BIT5, permissions, GROUP_READ);
                addIfBitSet(two, BIT6, permissions, OWNER_EXECUTE);
                addIfBitSet(two, BIT7, permissions, OWNER_WRITE);
            }

            if (three != 0)
                addIfBitSet(three, BIT0, permissions, OWNER_READ);

            if (!permissions.isEmpty())
                Files.setPosixFilePermissions(path, permissions);
        }

        private void addIfBitSet(int attr, int bit, Set<PosixFilePermission> permissions, PosixFilePermission permission) {
            if (BitUtils.isBitSet(attr, bit))
                permissions.add(permission);
        }
    };

    private final Predicate<String> is;

    public byte[] getAttributes(@NonNull Path path) throws IOException {
        return new byte[4];
    }

    public void applyAttribute(@NonNull Path path, byte[] attr) throws IOException {
    }

    public static OS current() {
        String os = System.getProperty("os.name").toLowerCase();

        for (OS system : values())
            if (system.is.test(os))
                return system;

        return UNKNOWN;
    }

}
