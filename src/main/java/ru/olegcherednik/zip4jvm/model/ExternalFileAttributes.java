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
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;

import java.io.IOException;
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

    private static final Supplier<String> PROP_OS_NAME = () -> Optional.ofNullable(System.getProperty("os.name"))
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
        osName = PROP_OS_NAME.get();
    }

    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public ExternalFileAttributes(Path path) {
        this();
        readFrom(path);
    }

    public ExternalFileAttributes(byte[] data) {
        this(data, PROP_OS_NAME.get());
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

    public void apply(Path path) throws IOException {
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

        public void apply(Path path, boolean posixReadOnly, boolean createdUnderPosix) throws IOException {
            DosFileAttributeView view = Files.getFileAttributeView(path, DosFileAttributeView.class);

            if (view == null)
                return;

            view.setReadOnly(createdUnderPosix ? posixReadOnly : readOnly);
            view.setHidden(!createdUnderPosix && hidden);
            view.setSystem(!createdUnderPosix && system);
            view.setArchive(createdUnderPosix || archive);
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

            if (attributes.isEmpty())
                return NONE;
            if (attributes.size() == 1 && "rdo".equals(attributes.get(0)))
                return "read-only";
            return String.join(" ", attributes);
        }

        @Override
        public String toString() {
            return "win";
        }

    }

    @Getter
    protected static class Posix {

        private boolean othersExecute;
        private boolean othersWrite;
        private boolean othersRead;
        private boolean groupExecute;
        private boolean groupWrite;
        private boolean groupRead;
        private boolean ownerExecute;
        private boolean ownerWrite;
        private boolean ownerRead;
        private Type type;

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
            othersExecute = permissions.contains(OTHERS_EXECUTE);
            othersWrite = permissions.contains(OTHERS_WRITE);
            othersRead = permissions.contains(OTHERS_READ);
            groupExecute = permissions.contains(GROUP_EXECUTE);
            groupWrite = permissions.contains(GROUP_WRITE);
            groupRead = permissions.contains(GROUP_READ);
            ownerExecute = permissions.contains(OWNER_EXECUTE);
            ownerWrite = permissions.contains(OWNER_WRITE);
            ownerRead = permissions.contains(OWNER_READ);
            type = Type.create(path);
        }

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
            type = Type.create(data[3]);
        }

        public void apply(Path path, boolean winReadOnly, boolean createdUnderPosix) throws IOException {
            Set<PosixFilePermission> permissions = EnumSet.noneOf(PosixFilePermission.class);

            addIfSet(!createdUnderPosix || othersExecute, permissions, OTHERS_EXECUTE);
            addIfSet(createdUnderPosix && othersWrite, permissions, OTHERS_WRITE);
            addIfSet(!createdUnderPosix || othersRead, permissions, OTHERS_READ);
            addIfSet(!createdUnderPosix || groupExecute, permissions, GROUP_EXECUTE);
            addIfSet(createdUnderPosix && groupWrite, permissions, GROUP_WRITE);
            addIfSet(!createdUnderPosix || groupRead, permissions, GROUP_READ);
            addIfSet(!createdUnderPosix || ownerExecute, permissions, OWNER_EXECUTE);
            addIfSet(!createdUnderPosix ? !winReadOnly : ownerWrite, permissions, OWNER_WRITE);
            addIfSet(!createdUnderPosix || ownerRead, permissions, OWNER_READ);

            Files.setPosixFilePermissions(path, permissions);
        }

        protected static void addIfSet(boolean exists,
                                       Set<PosixFilePermission> permissions,
                                       PosixFilePermission permission) {
            if (exists)
                permissions.add(permission);
        }

        public void fillData(byte[] data) {
            data[2] = BitUtils.updateBits(data[2], BIT0, othersExecute);
            data[2] = BitUtils.updateBits(data[2], BIT1, othersWrite);
            data[2] = BitUtils.updateBits(data[2], BIT2, othersRead);
            data[2] = BitUtils.updateBits(data[2], BIT3, groupExecute);
            data[2] = BitUtils.updateBits(data[2], BIT4, groupWrite);
            data[2] = BitUtils.updateBits(data[2], BIT5, groupRead);
            data[2] = BitUtils.updateBits(data[2], BIT6, ownerExecute);
            data[2] = BitUtils.updateBits(data[2], BIT7, ownerWrite);
            data[3] = BitUtils.updateBits(data[3], BIT0, ownerRead);
            data[3] = BitUtils.updateBits(data[3], BIT5, type == Type.SYMLINK);
            data[3] = BitUtils.updateBits(data[3], BIT6, type == Type.DIRECTORY);
            data[3] = BitUtils.updateBits(data[3], BIT7, type == Type.REGULAR_FILE || type == Type.SYMLINK);
        }

        public boolean isReadOnly() {
            return !othersWrite && !groupWrite && !ownerWrite;
        }

        public String getDetails() {
            String res = String.valueOf(type.getMarker())
                    + (ownerRead ? 'r' : '-')
                    + (ownerWrite ? 'w' : '-')
                    + (ownerExecute ? 'x' : '-')
                    + (groupRead ? 'r' : '-')
                    + (groupWrite ? 'w' : '-')
                    + (groupExecute ? 'x' : '-')
                    + (othersRead ? 'r' : '-')
                    + (othersWrite ? 'w' : '-')
                    + (othersExecute ? 'x' : '-');
            return "?---------".equals(res) ? NONE : res;
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

    }

}
