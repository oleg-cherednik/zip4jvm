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

import org.mockito.ArgumentCaptor;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.DosFileAttributes;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.spi.FileSystemProvider;
import java.util.Collections;
import java.util.Set;

import static java.nio.file.attribute.PosixFilePermission.GROUP_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.GROUP_READ;
import static java.nio.file.attribute.PosixFilePermission.GROUP_WRITE;
import static java.nio.file.attribute.PosixFilePermission.OTHERS_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.OTHERS_READ;
import static java.nio.file.attribute.PosixFilePermission.OTHERS_WRITE;
import static java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.OWNER_READ;
import static java.nio.file.attribute.PosixFilePermission.OWNER_WRITE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
 * @since 23.09.2019
 */
@Test
@SuppressWarnings("FieldNamingConvention")
public class ExternalFileAttributesTest {

    private static final int WINDOWS_READ_ONLY = BIT0;
    private static final int WINDOWS_HIDDEN = BIT1;
    private static final int WINDOWS_SYSTEM = BIT2;
    private static final int WINDOWS_LABORATORY = BIT3;
    private static final int WINDOWS_DIRECTORY = BIT4;
    private static final int WINDOWS_ARCHIVE = BIT5;

    private static final int POSIX_OTHERS_EXECUTE = BIT0;
    private static final int POSIX_OTHERS_WRITE = BIT1;
    private static final int POSIX_OTHERS_READ = BIT2;
    private static final int POSIX_GROUP_EXECUTE = BIT3;
    private static final int POSIX_GROUP_WRITE = BIT4;
    private static final int POSIX_GROUP_READ = BIT5;
    private static final int POSIX_OWNER_EXECUTE = BIT6;
    private static final int POSIX_OWNER_WRITE = BIT7;
    private static final int POSIX_OWNER_READ = BIT0;
    private static final int POSIX_DIRECTORY = BIT6;
    private static final int POSIX_REGULAR_FILE = BIT7;

    private static final Path rootDir = Zip4jvmSuite.generateSubDirNameWithTime(ExternalFileAttributesTest.class);

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jvmSuite.removeDir(rootDir);
    }

    public void shouldReadFromPathWhenWindows() throws IOException {
        Path path = mock(Path.class);
        FileSystem fileSystem = mock(FileSystem.class);
        FileSystemProvider provider = mock(FileSystemProvider.class);
        DosFileAttributeView fileAttributeView = mock(DosFileAttributeView.class);
        DosFileAttributes dos = mock(DosFileAttributes.class);

        when(path.getFileSystem()).thenReturn(fileSystem);
        when(fileSystem.provider()).thenReturn(provider);
        when(provider.getFileAttributeView(same(path), same(DosFileAttributeView.class), any(LinkOption[].class)))
                .thenReturn(fileAttributeView);
        when(fileAttributeView.readAttributes()).thenReturn(dos);

        when(dos.isReadOnly()).thenReturn(true);
        assertFileAttributes(path, 0, WINDOWS_READ_ONLY);
        reset(dos);

        when(dos.isHidden()).thenReturn(true);
        assertFileAttributes(path, 0, WINDOWS_HIDDEN);
        reset(dos);

        when(dos.isSystem()).thenReturn(true);
        assertFileAttributes(path, 0, WINDOWS_SYSTEM);
        reset(dos);

        when(dos.isArchive()).thenReturn(true);
        assertFileAttributes(path, 0, WINDOWS_ARCHIVE);
        reset(dos);
    }

    public void shouldReadFromPathWenPosix() throws IOException {
        Path path = mock(Path.class);
        FileSystem fileSystem = mock(FileSystem.class);
        FileSystemProvider provider = mock(FileSystemProvider.class);
        PosixFileAttributeView fileAttributeView = mock(PosixFileAttributeView.class);
        BasicFileAttributes basicFileAttributes = mock(BasicFileAttributes.class);
        PosixFileAttributes attributes = mock(PosixFileAttributes.class);

        when(path.getFileSystem()).thenReturn(fileSystem);
        when(fileSystem.provider()).thenReturn(provider);
        when(provider.getFileAttributeView(same(path), same(PosixFileAttributeView.class), any(LinkOption[].class)))
                .thenReturn(fileAttributeView);
        when(provider.readAttributes(same(path), same(BasicFileAttributes.class), any(LinkOption[].class)))
                .thenReturn(basicFileAttributes);
        when(fileAttributeView.readAttributes()).thenReturn(attributes);

        when(attributes.permissions()).thenReturn(Collections.singleton(OTHERS_EXECUTE));
        assertFileAttributes(path, 2, POSIX_OTHERS_EXECUTE);
        reset(attributes);

        when(attributes.permissions()).thenReturn(Collections.singleton(OTHERS_EXECUTE));
        assertFileAttributes(path, 2, POSIX_OTHERS_EXECUTE);
        reset(attributes);

        when(attributes.permissions()).thenReturn(Collections.singleton(OTHERS_WRITE));
        assertFileAttributes(path, 2, POSIX_OTHERS_WRITE);
        reset(attributes);

        when(attributes.permissions()).thenReturn(Collections.singleton(OTHERS_READ));
        assertFileAttributes(path, 2, POSIX_OTHERS_READ);
        reset(attributes);

        when(attributes.permissions()).thenReturn(Collections.singleton(GROUP_EXECUTE));
        assertFileAttributes(path, 2, POSIX_GROUP_EXECUTE);
        reset(attributes);

        when(attributes.permissions()).thenReturn(Collections.singleton(GROUP_WRITE));
        assertFileAttributes(path, 2, POSIX_GROUP_WRITE);
        reset(attributes);

        when(attributes.permissions()).thenReturn(Collections.singleton(GROUP_READ));
        assertFileAttributes(path, 2, POSIX_GROUP_READ);
        reset(attributes);

        when(attributes.permissions()).thenReturn(Collections.singleton(OWNER_EXECUTE));
        assertFileAttributes(path, 2, POSIX_OWNER_EXECUTE);
        reset(attributes);

        when(attributes.permissions()).thenReturn(Collections.singleton(OWNER_WRITE));
        assertFileAttributes(path, 2, POSIX_OWNER_WRITE);
        reset(attributes);

        when(attributes.permissions()).thenReturn(Collections.singleton(OWNER_READ));
        assertFileAttributes(path, 3, POSIX_OWNER_READ);
        reset(attributes);

        when(basicFileAttributes.isDirectory()).thenReturn(true);
        assertFileAttributes(path, 3, POSIX_DIRECTORY);
        reset(basicFileAttributes);

        when(basicFileAttributes.isRegularFile()).thenReturn(true);
        assertFileAttributes(path, 3, POSIX_REGULAR_FILE);
        reset(basicFileAttributes);
    }

    private static void assertFileAttributes(Path path, int pos, int expected) {
        ExternalFileAttributes externalFileAttributes = new ExternalFileAttributes(path);

        for (int i = 0; i < ExternalFileAttributes.SIZE; i++) {
            int b = externalFileAttributes.getData()[i] & 0xFF;
            assertThat(b).isEqualTo(i == pos ? expected : 0x0);
        }
    }

    public void shouldApplyPathWhenEntityUnderWindowsCreated() throws Exception {
        Path path = mock(Path.class);
        FileSystem fileSystem = mock(FileSystem.class);
        FileSystemProvider provider = mock(FileSystemProvider.class);
        DosFileAttributeView dos = mock(DosFileAttributeView.class);

        when(path.getFileSystem()).thenReturn(fileSystem);
        when(fileSystem.provider()).thenReturn(provider);
        when(provider.getFileAttributeView(same(path), same(DosFileAttributeView.class), any(LinkOption[].class)))
                .thenReturn(dos);

        new ExternalFileAttributes(new byte[] { WINDOWS_READ_ONLY, 0x0, 0x0, 0x0 },
                                   ExternalFileAttributes.WIN).apply(path);
        verify(dos, times(1)).setReadOnly(eq(true));
        reset(dos);

        new ExternalFileAttributes(new byte[] { WINDOWS_HIDDEN, 0x0, 0x0, 0x0 },
                                   ExternalFileAttributes.WIN).apply(path);
        verify(dos, times(1)).setHidden(eq(true));
        reset(dos);

        new ExternalFileAttributes(new byte[] { WINDOWS_SYSTEM, 0x0, 0x0, 0x0 },
                                   ExternalFileAttributes.WIN).apply(path);
        verify(dos, times(1)).setSystem(eq(true));
        reset(dos);

        new ExternalFileAttributes(new byte[] { WINDOWS_ARCHIVE, 0x0, 0x0, 0x0 },
                                   ExternalFileAttributes.WIN).apply(path);
        verify(dos, times(1)).setArchive(eq(true));
        reset(dos);

        new ExternalFileAttributes(new byte[] { WINDOWS_READ_ONLY | WINDOWS_ARCHIVE, 0x0, 0x0, 0x0 },
                                   ExternalFileAttributes.WIN).apply(path);
        verify(dos, times(1)).setReadOnly(eq(true));
        verify(dos, times(1)).setArchive(eq(true));
        reset(dos);
    }

    @Test
    public void shouldApplyPathWhenPosix() throws Exception {
        Path path = mock(Path.class);
        FileSystem fileSystem = mock(FileSystem.class);
        FileSystemProvider provider = mock(FileSystemProvider.class);
        PosixFileAttributeView fileAttributeView = mock(PosixFileAttributeView.class);
        BasicFileAttributes basicFileAttributes = mock(BasicFileAttributes.class);
        PosixFileAttributes attributes = mock(PosixFileAttributes.class);

        when(path.getFileSystem()).thenReturn(fileSystem);
        when(fileSystem.provider()).thenReturn(provider);
        when(provider.getFileAttributeView(same(path), same(PosixFileAttributeView.class), any(LinkOption[].class)))
                .thenReturn(fileAttributeView);
        when(provider.readAttributes(same(path),
                                     same(BasicFileAttributes.class),
                                     any())).thenReturn(basicFileAttributes);
        when(fileAttributeView.readAttributes()).thenReturn(attributes);

        Class<Set<PosixFilePermission>> listClass = (Class<Set<PosixFilePermission>>) (Class<?>) Set.class;
        ArgumentCaptor<Set<PosixFilePermission>> captor = ArgumentCaptor.forClass(listClass);

        new ExternalFileAttributes(new byte[] { 0x0, 0x0, POSIX_OTHERS_EXECUTE, 0x0 },
                                   ExternalFileAttributes.MAC).apply(path);
        verify(fileAttributeView).setPermissions(captor.capture());
        assertThat(captor.getValue()).containsExactly(OTHERS_EXECUTE);
        reset(fileAttributeView);

        new ExternalFileAttributes(new byte[] { 0x0, 0x0, POSIX_OTHERS_WRITE, 0x0 },
                                   ExternalFileAttributes.MAC).apply(path);
        verify(fileAttributeView).setPermissions(captor.capture());
        assertThat(captor.getValue()).containsExactly(OTHERS_WRITE);
        reset(fileAttributeView);

        new ExternalFileAttributes(new byte[] { 0x0, 0x0, POSIX_OTHERS_READ, 0x0 },
                                   ExternalFileAttributes.MAC).apply(path);
        verify(fileAttributeView).setPermissions(captor.capture());
        assertThat(captor.getValue()).containsExactly(OTHERS_READ);
        reset(fileAttributeView);

        new ExternalFileAttributes(new byte[] { 0x0, 0x0, POSIX_GROUP_EXECUTE, 0x0 },
                                   ExternalFileAttributes.MAC).apply(path);
        verify(fileAttributeView).setPermissions(captor.capture());
        assertThat(captor.getValue()).containsExactly(GROUP_EXECUTE);
        reset(fileAttributeView);

        new ExternalFileAttributes(new byte[] { 0x0, 0x0, POSIX_GROUP_WRITE, 0x0 },
                                   ExternalFileAttributes.MAC).apply(path);
        verify(fileAttributeView).setPermissions(captor.capture());
        assertThat(captor.getValue()).containsExactly(GROUP_WRITE);
        reset(fileAttributeView);

        new ExternalFileAttributes(new byte[] { 0x0, 0x0, POSIX_GROUP_READ, 0x0 },
                                   ExternalFileAttributes.MAC).apply(path);
        verify(fileAttributeView).setPermissions(captor.capture());
        assertThat(captor.getValue()).containsExactly(GROUP_READ);
        reset(fileAttributeView);

        new ExternalFileAttributes(new byte[] { 0x0, 0x0, POSIX_OWNER_EXECUTE, 0x0 },
                                   ExternalFileAttributes.MAC).apply(path);
        verify(fileAttributeView).setPermissions(captor.capture());
        assertThat(captor.getValue()).containsExactly(OWNER_EXECUTE);
        reset(fileAttributeView);

        new ExternalFileAttributes(new byte[] { 0x0, 0x0, (byte) POSIX_OWNER_WRITE, 0x0 },
                                   ExternalFileAttributes.MAC).apply(path);
        verify(fileAttributeView).setPermissions(captor.capture());
        assertThat(captor.getValue()).containsExactly(OWNER_WRITE);
        reset(fileAttributeView);

        new ExternalFileAttributes(new byte[] { 0x0, 0x0, 0x0, POSIX_OWNER_READ },
                                   ExternalFileAttributes.MAC).apply(path);
        verify(fileAttributeView).setPermissions(captor.capture());
        assertThat(captor.getValue()).containsExactly(OWNER_READ);
        reset(fileAttributeView);
    }

    public void shouldUseDefaultPermissionsForPosixWhenFileWasCreatedUnderWindows() throws Exception {
        Path path = mock(Path.class);
        FileSystem fileSystem = mock(FileSystem.class);
        FileSystemProvider provider = mock(FileSystemProvider.class);
        PosixFileAttributeView fileAttributeView = mock(PosixFileAttributeView.class);
        BasicFileAttributes basicFileAttributes = mock(BasicFileAttributes.class);
        PosixFileAttributes attributes = mock(PosixFileAttributes.class);

        when(path.getFileSystem()).thenReturn(fileSystem);
        when(fileSystem.provider()).thenReturn(provider);
        when(provider.getFileAttributeView(same(path), same(PosixFileAttributeView.class), any(LinkOption[].class)))
                .thenReturn(fileAttributeView);
        when(provider.readAttributes(same(path), same(BasicFileAttributes.class), any(LinkOption[].class)))
                .thenReturn(basicFileAttributes);
        when(fileAttributeView.readAttributes()).thenReturn(attributes);

        Class<Set<PosixFilePermission>> listClass = (Class<Set<PosixFilePermission>>) (Class<?>) Set.class;
        ArgumentCaptor<Set<PosixFilePermission>> captor = ArgumentCaptor.forClass(listClass);

        new ExternalFileAttributes(new byte[] { 0x0, 0x0, 0x0, 0x0 },
                                   ExternalFileAttributes.MAC).apply(path);
        verify(fileAttributeView).setPermissions(captor.capture());
        assertThat(captor.getValue()).containsExactlyInAnyOrder(OWNER_READ, OWNER_WRITE, OWNER_EXECUTE,
                                                                GROUP_READ, GROUP_EXECUTE,
                                                                OTHERS_READ, OTHERS_EXECUTE);
        reset(fileAttributeView);

        new ExternalFileAttributes(new byte[] { WINDOWS_READ_ONLY, 0x0, 0x0, 0x0 },
                                   ExternalFileAttributes.MAC).apply(path);
        verify(fileAttributeView).setPermissions(captor.capture());
        assertThat(captor.getValue()).containsExactlyInAnyOrder(OWNER_READ, OWNER_EXECUTE,
                                                                GROUP_READ, GROUP_EXECUTE,
                                                                OTHERS_READ, OTHERS_EXECUTE);
        reset(fileAttributeView);
    }

    public void shouldUseDefaultPermissionsForWindowsWhenFileWasCreatedUnderPosix() throws Exception {
        Path path = mock(Path.class);
        FileSystem fileSystem = mock(FileSystem.class);
        FileSystemProvider provider = mock(FileSystemProvider.class);
        DosFileAttributeView dos = mock(DosFileAttributeView.class);

        when(path.getFileSystem()).thenReturn(fileSystem);
        when(fileSystem.provider()).thenReturn(provider);
        when(provider.getFileAttributeView(same(path), same(DosFileAttributeView.class), any(LinkOption[].class)))
                .thenReturn(dos);

        new ExternalFileAttributes(new byte[] { 0x0, 0x0, 0x0, POSIX_OWNER_READ },
                                   ExternalFileAttributes.WIN).apply(path);

        verify(dos, times(1)).setReadOnly(eq(true));
        verify(dos, times(1)).setHidden(eq(false));
        verify(dos, times(1)).setSystem(eq(false));
        verify(dos, times(1)).setArchive(eq(true));
    }

    public void shouldRetrieveDetailsWhenWindowsFileSystem() {
        assertThat(new ExternalFileAttributes(new byte[] { 0x0, 0x0, 0x0, 0x0 }).getDetailsWin())
                .isEqualTo(ExternalFileAttributes.NONE);
        assertThat(new ExternalFileAttributes(new byte[] { WINDOWS_READ_ONLY, 0x0, 0x0, 0x0 }).getDetailsWin())
                .isEqualTo("read-only");
        assertThat(new ExternalFileAttributes(new byte[] { WINDOWS_HIDDEN, 0x0, 0x0, 0x0 }).getDetailsWin())
                .isEqualTo("hid");
        assertThat(new ExternalFileAttributes(new byte[] { WINDOWS_SYSTEM, 0x0, 0x0, 0x0 }).getDetailsWin())
                .isEqualTo("sys");
        assertThat(new ExternalFileAttributes(new byte[] { WINDOWS_LABORATORY, 0x0, 0x0, 0x0 }).getDetailsWin())
                .isEqualTo("lab");
        assertThat(new ExternalFileAttributes(new byte[] { WINDOWS_DIRECTORY, 0x0, 0x0, 0x0 }).getDetailsWin())
                .isEqualTo("dir");
        assertThat(new ExternalFileAttributes(new byte[] { WINDOWS_ARCHIVE, 0x0, 0x0, 0x0 }).getDetailsWin())
                .isEqualTo("arc");
        assertThat(new ExternalFileAttributes(new byte[] { WINDOWS_READ_ONLY | WINDOWS_SYSTEM, 0x0, 0x0, 0x0 })
                           .getDetailsWin())
                .isEqualTo("rdo sys");
    }

    public void shouldRetrieveDetailsWhenPosixFileSystem() {
        assertThat(new ExternalFileAttributes(new byte[] { 0x0, 0x0, 0x0, 0x0 }).getDetailsPosix()).isEqualTo(
                ExternalFileAttributes.NONE);
        assertThat(new ExternalFileAttributes(new byte[] { 0x0, 0x0, POSIX_OTHERS_EXECUTE,
                0x0 }).getDetailsPosix()).isEqualTo("?--------x");
        assertThat(new ExternalFileAttributes(new byte[] { 0x0, 0x0, POSIX_OTHERS_EXECUTE | POSIX_OTHERS_WRITE,
                0x0 }).getDetailsPosix())
                .isEqualTo("?-------wx");
        assertThat(new ExternalFileAttributes(new byte[] { 0x0, 0x0,
                POSIX_OTHERS_EXECUTE | POSIX_OTHERS_WRITE | POSIX_OTHERS_READ, 0x0 }).getDetailsPosix()).isEqualTo(
                "?------rwx");
        assertThat(new ExternalFileAttributes(new byte[] { 0x0, 0x0,
                POSIX_OTHERS_EXECUTE | POSIX_OTHERS_WRITE | POSIX_OTHERS_READ
                        | POSIX_GROUP_EXECUTE, 0x0 }).getDetailsPosix()).isEqualTo("?-----xrwx");
        assertThat(new ExternalFileAttributes(new byte[] { 0x0, 0x0,
                POSIX_OTHERS_EXECUTE | POSIX_OTHERS_WRITE | POSIX_OTHERS_READ
                        | POSIX_GROUP_EXECUTE | POSIX_GROUP_WRITE, 0x0 }).getDetailsPosix()).isEqualTo("?----wxrwx");
        assertThat(new ExternalFileAttributes(new byte[] { 0x0, 0x0,
                POSIX_OTHERS_EXECUTE | POSIX_OTHERS_WRITE | POSIX_OTHERS_READ
                        | POSIX_GROUP_EXECUTE | POSIX_GROUP_WRITE | POSIX_GROUP_READ,
                0x0 }).getDetailsPosix()).isEqualTo("?---rwxrwx");
        assertThat(new ExternalFileAttributes(new byte[] { 0x0, 0x0,
                POSIX_OTHERS_EXECUTE | POSIX_OTHERS_WRITE | POSIX_OTHERS_READ
                        | POSIX_GROUP_EXECUTE | POSIX_GROUP_WRITE | POSIX_GROUP_READ
                        | POSIX_OWNER_EXECUTE, 0x0 }).getDetailsPosix()).isEqualTo("?--xrwxrwx");
        assertThat(new ExternalFileAttributes(new byte[] { 0x0, 0x0,
                (byte) (POSIX_OTHERS_EXECUTE | POSIX_OTHERS_WRITE | POSIX_OTHERS_READ
                        | POSIX_GROUP_EXECUTE | POSIX_GROUP_WRITE | POSIX_GROUP_READ
                        | POSIX_OWNER_EXECUTE | POSIX_OWNER_WRITE), 0x0 }).getDetailsPosix()).isEqualTo("?-wxrwxrwx");
        assertThat(new ExternalFileAttributes(new byte[] { 0x0, 0x0,
                (byte) (POSIX_OTHERS_EXECUTE | POSIX_OTHERS_WRITE | POSIX_OTHERS_READ
                        | POSIX_GROUP_EXECUTE | POSIX_GROUP_WRITE | POSIX_GROUP_READ
                        | POSIX_OWNER_EXECUTE | POSIX_OWNER_WRITE),
                POSIX_OWNER_READ }).getDetailsPosix()).isEqualTo("?rwxrwxrwx");
        assertThat(new ExternalFileAttributes(new byte[] { 0x0, 0x0,
                (byte) (POSIX_OTHERS_EXECUTE | POSIX_OTHERS_WRITE | POSIX_OTHERS_READ
                        | POSIX_GROUP_EXECUTE | POSIX_GROUP_WRITE | POSIX_GROUP_READ
                        | POSIX_OWNER_EXECUTE | POSIX_OWNER_WRITE),
                POSIX_OWNER_READ | POSIX_DIRECTORY }).getDetailsPosix()).isEqualTo("drwxrwxrwx");
        assertThat(new ExternalFileAttributes(new byte[] { 0x0, 0x0,
                (byte) (POSIX_OTHERS_EXECUTE | POSIX_OTHERS_WRITE | POSIX_OTHERS_READ
                        | POSIX_GROUP_EXECUTE | POSIX_GROUP_WRITE | POSIX_GROUP_READ
                        | POSIX_OWNER_EXECUTE | POSIX_OWNER_WRITE),
                (byte) (POSIX_OWNER_READ | POSIX_REGULAR_FILE) }).getDetailsPosix()).isEqualTo("-rwxrwxrwx");
    }

}

