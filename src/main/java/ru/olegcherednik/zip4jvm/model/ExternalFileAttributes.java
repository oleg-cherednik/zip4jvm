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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import ru.olegcherednik.zip4jvm.utils.BitUtils;

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
public abstract class ExternalFileAttributes {

    public static final Supplier<String> PROP_OS_NAME = () -> System.getProperty("os.name");
    public static final String NONE = "none";

    public static final String WIN = "win";
    public static final String MAC = "mac";
    public static final String UNIX = "nux";

    @SuppressWarnings("StaticInitializerReferencesSubClass")
    public static final ExternalFileAttributes NULL = new Unknown();
    public static final int SIZE = 4;

    protected final byte[] data = new byte[SIZE];

    public static ExternalFileAttributes build(Supplier<String> osNameProvider) {
        String os = Optional.ofNullable(osNameProvider).orElse(() -> "<unknown>").get().toLowerCase();

        if (os.contains(WIN))
            return new Windows();
        if (os.contains(MAC) || os.contains(UNIX))
            return new Posix();

        return NULL;
    }

    public ExternalFileAttributes readFrom(Path path) throws IOException {
        // clear, because use local file system
        Arrays.fill(data, (byte)0x0);
        return this;
    }

    public ExternalFileAttributes readFrom(byte[] data) {
        // copy, because read from archive metadata
        System.arraycopy(data, 0, this.data, 0, SIZE);
        return this;
    }

    public abstract void apply(Path path) throws IOException;

    public byte[] getData() {
        return ArrayUtils.clone(data);
    }

    public abstract String getDetails();

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
        public String getDetails() {
            return "none";
        }

        @Override
        public String toString() {
            return "<null>";
        }

    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    private static class Windows extends ExternalFileAttributes {

        private boolean readOnly;
        private boolean hidden;
        private boolean system;
        private boolean archive;
        private boolean laboratory;
        private boolean directory;

        public static boolean isWindows(byte[] data) {
            return data[0] != 0;
        }

        @Override
        public Windows readFrom(Path path) throws IOException {
            super.readFrom(path);

            DosFileAttributeView view = Files.getFileAttributeView(path, DosFileAttributeView.class);

            if (view != null) {
                DosFileAttributes dos = view.readAttributes();
                readOnly = dos.isReadOnly();
                hidden = dos.isHidden();
                system = dos.isSystem();
                archive = dos.isArchive();
                directory = dos.isDirectory();
            }

            return this;
        }

        @Override
        public Windows readFrom(byte[] data) {
            super.readFrom(data);

            if (isWindows(data)) {
                readOnly = isReadOnly(data);
                hidden = BitUtils.isBitSet(data[0], BIT1);
                system = BitUtils.isBitSet(data[0], BIT2);
                laboratory = BitUtils.isBitSet(data[0], BIT3);
                directory = BitUtils.isBitSet(data[0], BIT4);
                archive = BitUtils.isBitSet(data[0], BIT5);
            }

            return this;
        }

        @Override
        @SuppressWarnings("SimplifiableConditionalExpression")
        public void apply(Path path) throws IOException {
            boolean windows = isWindows(data);

            DosFileAttributeView view = Files.getFileAttributeView(path, DosFileAttributeView.class);

            if (view != null) {
                view.setReadOnly(windows ? readOnly : Posix.isReadOnly(data));
                view.setHidden(windows ? hidden : false);
                view.setSystem(windows ? system : false);
                view.setArchive(windows ? archive : true);
            }
        }

        @Override
        public byte[] getData() {
            byte[] data = super.getData();

            data[0] = BitUtils.updateBits((byte)0x0, BIT0, readOnly);
            data[0] = BitUtils.updateBits(data[0], BIT1, hidden);
            data[0] = BitUtils.updateBits(data[0], BIT2, system);
            data[0] = BitUtils.updateBits(data[0], BIT3, laboratory);
            data[0] = BitUtils.updateBits(data[0], BIT4, directory);
            data[0] = BitUtils.updateBits(data[0], BIT5, archive);

            return data;
        }

        @Override
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

        private static boolean isReadOnly(byte[] data) {
            return BitUtils.isBitSet(data[0], BIT0);
        }

    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
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
        private boolean symlink;
        private boolean directory;
        private boolean regularFile;

        public static boolean isPosix(byte[] data) {
            return data[2] != 0 || data[3] != 0;
        }

        @Override
        public Posix readFrom(Path path) throws IOException {
            super.readFrom(path);

            PosixFileAttributeView view = Files.getFileAttributeView(path, PosixFileAttributeView.class);

            if (view != null) {
                Set<PosixFilePermission> permissions = view.readAttributes().permissions();
                othersExecute = permissions.contains(OTHERS_EXECUTE);
                othersWrite = permissions.contains(OTHERS_WRITE);
                othersRead = permissions.contains(OTHERS_READ);
                groupExecute = permissions.contains(GROUP_EXECUTE);
                groupWrite = permissions.contains(GROUP_WRITE);
                groupRead = permissions.contains(GROUP_READ);
                ownerExecute = permissions.contains(OWNER_EXECUTE);
                ownerWrite = permissions.contains(OWNER_WRITE);
                ownerRead = permissions.contains(OWNER_READ);
                symlink = Files.isSymbolicLink(path);
                directory = Files.isDirectory(path);
                regularFile = Files.isRegularFile(path);
            }

            return this;
        }

        @Override
        public Posix readFrom(byte[] data) {
            super.readFrom(data);

            if (isPosix(data)) {
                othersExecute = BitUtils.isBitSet(data[2], BIT0);
                othersWrite = BitUtils.isBitSet(data[2], BIT1);
                othersRead = BitUtils.isBitSet(data[2], BIT2);
                groupExecute = BitUtils.isBitSet(data[2], BIT3);
                groupWrite = BitUtils.isBitSet(data[2], BIT4);
                groupRead = BitUtils.isBitSet(data[2], BIT5);
                ownerExecute = BitUtils.isBitSet(data[2], BIT6);
                ownerWrite = BitUtils.isBitSet(data[2], BIT7);
                ownerRead = BitUtils.isBitSet(data[3], BIT0);
                symlink = BitUtils.isBitSet(data[3], BIT5 | BIT7);
                directory = BitUtils.isBitSet(data[3], BIT6);
                regularFile = BitUtils.isBitSet(data[3], BIT7) && BitUtils.isBitClear(data[3], BIT5);
            }

            return this;
        }

        @Override
        @SuppressWarnings("SimplifiableConditionalExpression")
        public void apply(Path path) throws IOException {
            boolean posix = isPosix(data);
            Set<PosixFilePermission> permissions = EnumSet.noneOf(PosixFilePermission.class);

            addIfSet(posix ? othersExecute : false, permissions, OTHERS_EXECUTE);
            addIfSet(posix ? othersWrite : false, permissions, OTHERS_WRITE);
            addIfSet(posix ? othersRead : true, permissions, OTHERS_READ);
            addIfSet(posix ? groupExecute : false, permissions, GROUP_EXECUTE);
            addIfSet(posix ? groupWrite : false, permissions, GROUP_WRITE);
            addIfSet(posix ? groupRead : true, permissions, GROUP_READ);
            addIfSet(posix ? ownerExecute : false, permissions, OWNER_EXECUTE);
            addIfSet(posix ? ownerWrite : !Windows.isReadOnly(data), permissions, OWNER_WRITE);
            addIfSet(posix ? ownerRead : true, permissions, OWNER_READ);

            Files.setPosixFilePermissions(path, permissions);
        }

        @Override
        public byte[] getData() {
            byte[] data = super.getData();

            data[2] = BitUtils.updateBits((byte)0x0, BIT0, othersExecute);
            data[2] = BitUtils.updateBits(data[2], BIT1, othersWrite);
            data[2] = BitUtils.updateBits(data[2], BIT2, othersRead);
            data[2] = BitUtils.updateBits(data[2], BIT3, groupExecute);
            data[2] = BitUtils.updateBits(data[2], BIT4, groupWrite);
            data[2] = BitUtils.updateBits(data[2], BIT5, groupRead);
            data[2] = BitUtils.updateBits(data[2], BIT6, ownerExecute);
            data[2] = BitUtils.updateBits(data[2], BIT7, ownerWrite);
            data[3] = BitUtils.updateBits((byte)0x0, BIT0, ownerRead);
            data[3] = BitUtils.updateBits(data[3], BIT5, symlink);
            data[3] = BitUtils.updateBits(data[3], BIT6, directory);
            data[3] = BitUtils.updateBits(data[3], BIT7, regularFile || symlink);

            return data;
        }

        @Override
        public String getDetails() {
            StringBuilder buf = new StringBuilder();

            if (directory)
                buf.append('d');
            else if (symlink)
                buf.append('l');
            else if (regularFile)
                buf.append('-');
            else
                buf.append('?');

            buf.append(ownerRead ? 'r' : '-');
            buf.append(ownerWrite ? 'w' : '-');
            buf.append(ownerExecute ? 'x' : '-');

            buf.append(groupRead ? 'r' : '-');
            buf.append(groupWrite ? 'w' : '-');
            buf.append(groupExecute ? 'x' : '-');

            buf.append(othersRead ? 'r' : '-');
            buf.append(othersWrite ? 'w' : '-');
            buf.append(othersExecute ? 'x' : '-');

            String res = buf.toString();
            return "?---------".equals(res) ? NONE : res;
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
