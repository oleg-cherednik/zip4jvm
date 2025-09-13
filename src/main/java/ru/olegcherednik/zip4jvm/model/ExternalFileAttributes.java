/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package ru.olegcherednik.zip4jvm.model;

import ru.olegcherednik.zip4jvm.utils.BitUtils;
import ru.olegcherednik.zip4jvm.utils.PathUtils;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.DosFileAttributes;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
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
public class ExternalFileAttributes {

    public static final int SIZE = 4;

    private static final Supplier<String> GET_OS_NAME_FROM_PROPERTIES =
            () -> Optional.ofNullable(System.getProperty("os.name"))
                          .orElse("<unknown>")
                          .toLowerCase(Locale.ENGLISH);

    public static final String WIN = "win";
    public static final String MAC = "mac";
    public static final String UNIX = "nux";
    public static final String NONE = "none";

    protected final byte[] data = new byte[SIZE];

    private final String osName;
    private final Windows windows = new Windows();
    private final Posix posix = new Posix();

    public ExternalFileAttributes() {
        osName = GET_OS_NAME_FROM_PROPERTIES.get();
    }

    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public ExternalFileAttributes(Path path) {
        this();
        readFrom(path);
    }

    public ExternalFileAttributes(byte[] data) {
        this(data, GET_OS_NAME_FROM_PROPERTIES.get());
    }

    ExternalFileAttributes(byte[] data, String osName) {
        this.osName = osName;
        readFrom(data);
    }

    public static ExternalFileAttributes symlink(Path path) {
        return new ExternalFileAttributes(path).readFrom(path).symlink();
    }

    public static ExternalFileAttributes directory(Path path) {
        return new ExternalFileAttributes(path).readFrom(path).directory();
    }

    public static ExternalFileAttributes regularFile(Path path) {
        return new ExternalFileAttributes(path).readFrom(path).regularFile();
    }

    public boolean isSymlink() {
        return windows.isSymlink() || posix.isSymlink();
    }

    public ExternalFileAttributes readFrom(Path path) {
        // clear, because use local file system
        Arrays.fill(data, (byte) 0x0);

        posix.readFrom(path);
        windows.readFrom(path);

        return this;
    }

    private ExternalFileAttributes readFrom(byte[] data) {
        // copy, because read from archive metadata
        System.arraycopy(data, 0, this.data, 0, this.data.length);

        posix.readFrom(data);
        windows.readFrom(data, posix.isSymlink());

        return this;
    }

    private ExternalFileAttributes symlink() {
        posix.markAsSymlink();
        windows.markAsSymlink();
        return this;
    }

    private ExternalFileAttributes directory() {
        posix.markAsDirectory();
        windows.markAsDirectory();
        return this;
    }

    private ExternalFileAttributes regularFile() {
        posix.markAsFile();
        windows.markAsRegularFile();
        return this;
    }

    public void apply(Path path) {
        boolean createdUnderPosix = data[2] != 0 || data[3] != 0;

        if (osName.contains(WIN))
            windows.apply(path, posix.isReadOnly(), createdUnderPosix);
        else if (osName.contains(MAC) || osName.contains(UNIX))
            posix.apply(path, windows.isReadOnly(), createdUnderPosix);
    }

    public byte[] getData() {
        byte[] buf = ArrayUtils.clone(data);
        windows.fillData(buf);
        posix.fillData(buf);
        return buf;
    }

    public String getDetailsWin() {
        return windows.getDetails();
    }

    public String getDetailsPosix() {
        return posix.getDetails();
    }

    @Getter
    protected static class Windows {

        private boolean readOnly;
        private boolean hidden;
        private boolean system;
        private boolean laboratory;
        private boolean archive;
        private boolean directory;
        private boolean regularFile;
        private boolean symlink;

        private void markAsSymlink() {
            symlink = true;
            directory = false;
            regularFile = false;
        }

        private void markAsDirectory() {
            symlink = false;
            directory = true;
            regularFile = false;
        }

        private void markAsRegularFile() {
            symlink = false;
            directory = false;
            regularFile = true;
        }

        public void readFrom(Path path) {
            DosFileAttributeView view = Files.getFileAttributeView(path, DosFileAttributeView.class);

            if (view != null) {
                DosFileAttributes dos = Quietly.doRuntime(view::readAttributes);
                readOnly = dos.isReadOnly();
                hidden = dos.isHidden();
                system = dos.isSystem();
                archive = dos.isArchive();
                directory = dos.isDirectory();
                regularFile = dos.isRegularFile();
                symlink = dos.isSymbolicLink();
            }
        }

        public void readFrom(byte[] data, boolean symlink) {
            readOnly = BitUtils.isBitSet(data[0], BIT0);
            hidden = BitUtils.isBitSet(data[0], BIT1);
            system = BitUtils.isBitSet(data[0], BIT2);
            laboratory = BitUtils.isBitSet(data[0], BIT3);
            directory = BitUtils.isBitSet(data[0], BIT4);
            regularFile = BitUtils.isBitClear(data[0], BIT4) && !symlink;
            archive = BitUtils.isBitSet(data[0], BIT5);
            this.symlink = symlink;
        }

        public void apply(Path path, boolean posixReadOnly, boolean createdUnderPosix) {
            DosFileAttributeView view = Files.getFileAttributeView(path, DosFileAttributeView.class);

            if (view == null)
                return;

            Quietly.doRuntime(() -> {
                view.setReadOnly(createdUnderPosix ? posixReadOnly : readOnly);
                view.setHidden(!createdUnderPosix && hidden);
                view.setSystem(!createdUnderPosix && system);
                view.setArchive(createdUnderPosix || archive);
            });
        }

        public void fillData(byte[] data) {
            data[0] = BitUtils.updateBits((byte) 0x0, BIT0, readOnly);
            data[0] = BitUtils.updateBits(data[0], BIT1, hidden);
            data[0] = BitUtils.updateBits(data[0], BIT2, system);
            data[0] = BitUtils.updateBits(data[0], BIT3, laboratory);
            data[0] = BitUtils.updateBits(data[0], BIT4, directory);
            data[0] = BitUtils.updateBits(data[0], BIT5, archive);
        }

        public String getDetails() {
            List<String> attributes = getAttributes();

            if (attributes.isEmpty())
                return NONE;
            if (attributes.size() == 1 && "rdo".equals(attributes.get(0)))
                return "read-only";
            return String.join(" ", attributes);
        }

        private List<String> getAttributes() {
            List<String> attributes = new ArrayList<>(4);

            if (readOnly)
                attributes.add("rdo");
            if (hidden)
                attributes.add("hid");
            if (system)
                attributes.add("sys");
            if (laboratory)
                attributes.add("lab");
            if (directory)
                attributes.add("dir");
            if (archive)
                attributes.add("arc");

            return attributes;
        }

        @Override
        public String toString() {
            return "win";
        }

    }

    @Getter
    protected static class Posix {

        private final Permission owner = new Permission();
        private final Permission group = new Permission();
        private final Permission others = new Permission();
        private Type type = Type.UNKNOWN;

        public boolean isSymlink() {
            return type == Type.SYMLINK;
        }

        public boolean isDirectory() {
            return type == Type.DIRECTORY;
        }

        public boolean isRegularFile() {
            return type == Type.REGULAR_FILE;
        }

        private void markAsSymlink() {
            type = Type.SYMLINK;
        }

        private void markAsDirectory() {
            type = Type.DIRECTORY;
        }

        private void markAsFile() {
            type = Type.REGULAR_FILE;
        }

        public void readFrom(Path path) {
            PosixFileAttributeView view = Files.getFileAttributeView(path, PosixFileAttributeView.class);

            if (view == null)
                return;

            Set<PosixFilePermission> permissions = Quietly.doRuntime(() -> view.readAttributes().permissions());
            owner.execute = permissions.contains(OWNER_EXECUTE);
            owner.write = permissions.contains(OWNER_WRITE);
            owner.read = permissions.contains(OWNER_READ);

            group.execute = permissions.contains(GROUP_EXECUTE);
            group.write = permissions.contains(GROUP_WRITE);
            group.read = permissions.contains(GROUP_READ);

            others.execute = permissions.contains(OTHERS_EXECUTE);
            others.write = permissions.contains(OTHERS_WRITE);
            others.read = permissions.contains(OTHERS_READ);

            type = Type.create(path);
        }

        public void readFrom(byte[] data) {
            others.execute = BitUtils.isBitSet(data[2], BIT0);
            others.write = BitUtils.isBitSet(data[2], BIT1);
            others.read = BitUtils.isBitSet(data[2], BIT2);

            group.execute = BitUtils.isBitSet(data[2], BIT3);
            group.write = BitUtils.isBitSet(data[2], BIT4);
            group.read = BitUtils.isBitSet(data[2], BIT5);

            owner.execute = BitUtils.isBitSet(data[2], BIT6);
            owner.write = BitUtils.isBitSet(data[2], BIT7);
            owner.read = BitUtils.isBitSet(data[3], BIT0);

            type = Type.create(data[3]);
        }

        public void apply(Path path, boolean winReadOnly, boolean createdUnderPosix) {
            Set<PosixFilePermission> permissions = EnumSet.noneOf(PosixFilePermission.class);

            addIfSet(!createdUnderPosix || others.execute, permissions, OTHERS_EXECUTE);
            addIfSet(createdUnderPosix && others.write, permissions, OTHERS_WRITE);
            addIfSet(!createdUnderPosix || others.read, permissions, OTHERS_READ);

            addIfSet(!createdUnderPosix || group.execute, permissions, GROUP_EXECUTE);
            addIfSet(createdUnderPosix && group.write, permissions, GROUP_WRITE);
            addIfSet(!createdUnderPosix || group.read, permissions, GROUP_READ);

            addIfSet(!createdUnderPosix || owner.execute, permissions, OWNER_EXECUTE);
            addIfSet(!createdUnderPosix ? !winReadOnly : owner.write, permissions, OWNER_WRITE);
            addIfSet(!createdUnderPosix || owner.read, permissions, OWNER_READ);

            PathUtils.setPosixFilePermissions(path, permissions);
        }

        protected static void addIfSet(boolean exists,
                                       Set<PosixFilePermission> permissions,
                                       PosixFilePermission permission) {
            if (exists)
                permissions.add(permission);
        }

        public void fillData(byte[] data) {
            data[2] = BitUtils.updateBits(data[2], BIT0, others.execute);
            data[2] = BitUtils.updateBits(data[2], BIT1, others.write);
            data[2] = BitUtils.updateBits(data[2], BIT2, others.read);

            data[2] = BitUtils.updateBits(data[2], BIT3, group.execute);
            data[2] = BitUtils.updateBits(data[2], BIT4, group.write);
            data[2] = BitUtils.updateBits(data[2], BIT5, group.read);

            data[2] = BitUtils.updateBits(data[2], BIT6, owner.execute);
            data[2] = BitUtils.updateBits(data[2], BIT7, owner.write);
            data[3] = BitUtils.updateBits(data[3], BIT0, owner.read);

            data[3] = BitUtils.updateBits(data[3], BIT5, type == Type.SYMLINK);
            data[3] = BitUtils.updateBits(data[3], BIT6, type == Type.DIRECTORY);
            data[3] = BitUtils.updateBits(data[3], BIT7, type == Type.REGULAR_FILE || type == Type.SYMLINK);
        }

        public boolean isReadOnly() {
            return !others.write && !group.write && !owner.write;
        }

        public String getDetails() {
            if (type == Type.UNKNOWN && owner.isEmpty() && group.isEmpty() && others.isEmpty())
                return NONE;
            return String.valueOf(type.getMarker()) + owner + group + others;
        }

        @Override
        public String toString() {
            return "posix";
        }

        @Getter
        @RequiredArgsConstructor(access = AccessLevel.PACKAGE)
        private enum Type {
            SYMLINK('l'),
            DIRECTORY('d'),
            REGULAR_FILE('-'),
            UNKNOWN('?');

            private final char marker;

            public static Type create(Path path) {
                if (Files.isSymbolicLink(path))
                    return SYMLINK;
                if (Files.isDirectory(path))
                    return DIRECTORY;
                if (Files.isRegularFile(path))
                    return REGULAR_FILE;
                return UNKNOWN;
            }

            public static Type create(byte data) {
                if (BitUtils.isBitSet(data, BIT5 | BIT7))
                    return SYMLINK;
                if (BitUtils.isBitSet(data, BIT6))
                    return DIRECTORY;
                if (BitUtils.isBitSet(data, BIT7) && BitUtils.isBitClear(data, BIT5))
                    return REGULAR_FILE;
                return UNKNOWN;
            }
        }

        @RequiredArgsConstructor
        private static final class Permission {

            private boolean read;
            private boolean write;
            private boolean execute;

            public boolean isEmpty() {
                return !read && !write && !execute;
            }

            @Override
            public String toString() {
                return String.valueOf(read ? 'r' : '-') + (write ? 'w' : '-') + (execute ? 'x' : '-');
            }

        }

    }

}
