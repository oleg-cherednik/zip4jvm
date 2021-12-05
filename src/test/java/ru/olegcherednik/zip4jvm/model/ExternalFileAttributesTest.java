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
import ru.olegcherednik.zip4jvm.utils.BitUtils;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.DosFileAttributes;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.spi.FileSystemProvider;
import java.util.Arrays;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.olegcherednik.zip4jvm.TestData.fileBentley;
import static ru.olegcherednik.zip4jvm.model.ExternalFileAttributes.MAC;
import static ru.olegcherednik.zip4jvm.model.ExternalFileAttributes.UNIX;
import static ru.olegcherednik.zip4jvm.model.ExternalFileAttributes.WIN;
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

    public void shouldRetrieveImplementationWhenUseSupplier() {
        assertThat(ExternalFileAttributes.build(() -> WIN).toString()).isEqualTo("win");
        assertThat(ExternalFileAttributes.build(() -> MAC).toString()).isEqualTo("posix");
        assertThat(ExternalFileAttributes.build(() -> UNIX).toString()).isEqualTo("posix");
        assertThat(ExternalFileAttributes.NULL.toString()).isEqualTo("<null>");
        assertThat(ExternalFileAttributes.build(() -> "<unknown>")).isSameAs(ExternalFileAttributes.NULL);
        assertThat(ExternalFileAttributes.build(null)).isSameAs(ExternalFileAttributes.NULL);
    }

    public void shouldReadFromPathWenWindows() throws IOException {
        Path path = mock(Path.class);
        FileSystem fileSystem = mock(FileSystem.class);
        FileSystemProvider provider = mock(FileSystemProvider.class);
        DosFileAttributeView fileAttributeView = mock(DosFileAttributeView.class);
        DosFileAttributes dos = mock(DosFileAttributes.class);

        when(path.getFileSystem()).thenReturn(fileSystem);
        when(fileSystem.provider()).thenReturn(provider);
        when(provider.getFileAttributeView(same(path), same(DosFileAttributeView.class), any())).thenReturn(fileAttributeView);
        when(fileAttributeView.readAttributes()).thenReturn(dos);

        ExternalFileAttributes externalFileAttributes = ExternalFileAttributes.build(() -> WIN);

        when(dos.isReadOnly()).thenReturn(true);
        externalFileAttributes.readFrom(path);
        assertThat(externalFileAttributes.getData()[0] & 0xFF).isEqualTo(WINDOWS_READ_ONLY);
        reset(dos);

        when(dos.isHidden()).thenReturn(true);
        externalFileAttributes.readFrom(path);
        assertThat(externalFileAttributes.getData()[0] & 0xFF).isEqualTo(WINDOWS_HIDDEN);
        reset(dos);

        when(dos.isSystem()).thenReturn(true);
        externalFileAttributes.readFrom(path);
        assertThat(externalFileAttributes.getData()[0] & 0xFF).isEqualTo(WINDOWS_SYSTEM);
        reset(dos);

        when(dos.isArchive()).thenReturn(true);
        externalFileAttributes.readFrom(path);
        assertThat(externalFileAttributes.getData()[0] & 0xFF).isEqualTo(WINDOWS_ARCHIVE);
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
        when(provider.getFileAttributeView(same(path), same(PosixFileAttributeView.class), any())).thenReturn(fileAttributeView);
        when(provider.readAttributes(same(path), same(BasicFileAttributes.class), any())).thenReturn(basicFileAttributes);
        when(fileAttributeView.readAttributes()).thenReturn(attributes);

        ExternalFileAttributes externalFileAttributes = ExternalFileAttributes.build(() -> MAC);

        when(attributes.permissions()).thenReturn(Collections.singleton(OTHERS_EXECUTE));
        externalFileAttributes.readFrom(path);
        assertThat(externalFileAttributes.getData()[2] & 0xFF).isEqualTo(POSIX_OTHERS_EXECUTE);
        reset(attributes);

        when(attributes.permissions()).thenReturn(Collections.singleton(OTHERS_WRITE));
        externalFileAttributes.readFrom(path);
        assertThat(externalFileAttributes.getData()[2] & 0xFF).isEqualTo(POSIX_OTHERS_WRITE);
        reset(attributes);

        when(attributes.permissions()).thenReturn(Collections.singleton(OTHERS_READ));
        externalFileAttributes.readFrom(path);
        assertThat(externalFileAttributes.getData()[2] & 0xFF).isEqualTo(POSIX_OTHERS_READ);
        reset(attributes);

        when(attributes.permissions()).thenReturn(Collections.singleton(GROUP_EXECUTE));
        externalFileAttributes.readFrom(path);
        assertThat(externalFileAttributes.getData()[2] & 0xFF).isEqualTo(POSIX_GROUP_EXECUTE);
        reset(attributes);

        when(attributes.permissions()).thenReturn(Collections.singleton(GROUP_WRITE));
        externalFileAttributes.readFrom(path);
        assertThat(externalFileAttributes.getData()[2] & 0xFF).isEqualTo(POSIX_GROUP_WRITE);
        reset(attributes);

        when(attributes.permissions()).thenReturn(Collections.singleton(GROUP_READ));
        externalFileAttributes.readFrom(path);
        assertThat(externalFileAttributes.getData()[2] & 0xFF).isEqualTo(POSIX_GROUP_READ);
        reset(attributes);

        when(attributes.permissions()).thenReturn(Collections.singleton(OWNER_EXECUTE));
        externalFileAttributes.readFrom(path);
        assertThat(externalFileAttributes.getData()[2] & 0xFF).isEqualTo(POSIX_OWNER_EXECUTE);
        reset(attributes);

        when(attributes.permissions()).thenReturn(Collections.singleton(OWNER_WRITE));
        externalFileAttributes.readFrom(path);
        assertThat(externalFileAttributes.getData()[2] & 0xFF).isEqualTo(POSIX_OWNER_WRITE);
        reset(attributes);

        when(attributes.permissions()).thenReturn(Collections.singleton(OWNER_READ));
        externalFileAttributes.readFrom(path);
        assertThat(externalFileAttributes.getData()[3] & 0xFF).isEqualTo(POSIX_OWNER_READ);
        reset(attributes);

        when(basicFileAttributes.isDirectory()).thenReturn(true);
        externalFileAttributes.readFrom(path);
        assertThat(externalFileAttributes.getData()[3] & 0xFF).isEqualTo(POSIX_DIRECTORY);
        reset(basicFileAttributes);

        when(basicFileAttributes.isRegularFile()).thenReturn(true);
        externalFileAttributes.readFrom(path);
        assertThat(externalFileAttributes.getData()[3] & 0xFF).isEqualTo(POSIX_REGULAR_FILE);
        reset(basicFileAttributes);
    }

    public void shouldRetrievePosixInstanceWhenMacOrUnixSystem() throws IOException {
        Path path = mock(Path.class);
        FileSystem fileSystem = mock(FileSystem.class);
        FileSystemProvider provider = mock(FileSystemProvider.class);
        PosixFileAttributeView fileAttributeView = mock(PosixFileAttributeView.class);
        BasicFileAttributes basicFileAttributes = mock(BasicFileAttributes.class);
        PosixFileAttributes attributes = mock(PosixFileAttributes.class);

        when(path.getFileSystem()).thenReturn(fileSystem);
        when(fileSystem.provider()).thenReturn(provider);
        when(provider.getFileAttributeView(same(path), same(PosixFileAttributeView.class), any())).thenReturn(fileAttributeView);
        when(provider.readAttributes(same(path), same(BasicFileAttributes.class), any())).thenReturn(basicFileAttributes);
        when(fileAttributeView.readAttributes()).thenReturn(attributes);
        when(basicFileAttributes.isDirectory()).thenReturn(false);
        when(basicFileAttributes.isRegularFile()).thenReturn(true);
        when(attributes.permissions()).thenReturn(Collections.emptySet());

        for (String osName : Arrays.asList(MAC, UNIX)) {
            ExternalFileAttributes externalFileAttributes = ExternalFileAttributes.build(() -> osName).readFrom(path);
            assertThat(externalFileAttributes.toString()).isEqualTo("posix");
        }
    }

    public void shouldIgnoreReadFromByteArrayFromWhenNullObject() {
        ExternalFileAttributes externalFileAttributes = ExternalFileAttributes.NULL;
        externalFileAttributes.readFrom(new byte[] { 0xA, 0xA, 0xA, 0xA });
        assertThat(externalFileAttributes.getData()).isEqualTo(new byte[] { 0x0, 0x0, 0x0, 0x0 });
    }

    public void shouldIgnoreReadFromPathNullObject() throws IOException {
        ExternalFileAttributes externalFileAttributes = ExternalFileAttributes.NULL;
        externalFileAttributes.readFrom(fileBentley);
        assertThat(externalFileAttributes.getData()).isEqualTo(new byte[] { 0x0, 0x0, 0x0, 0x0 });
    }

    public void shouldIgnoreApplyPathWhenNullObject() throws IOException {
        Path path = mock(Path.class);
        ExternalFileAttributes.NULL.apply(fileBentley);
        verify(path, never()).getFileSystem();
    }

    public void shouldReadFromByteArrayWhenWindows() {
        ExternalFileAttributes externalFileAttributes = ExternalFileAttributes.build(() -> WIN);

        for (int b0 : Arrays.asList(WINDOWS_READ_ONLY, WINDOWS_HIDDEN, WINDOWS_SYSTEM, WINDOWS_ARCHIVE)) {
            externalFileAttributes.readFrom(new byte[] { (byte)b0, 0x0, 0x0, 0x0 });
            assertThat(externalFileAttributes.getData()[0] & 0xFF).isEqualTo(b0);
        }

        externalFileAttributes.readFrom(new byte[] { 0x0, 0x0,
                (byte)BitUtils.clearBits(0xFF, POSIX_OTHERS_WRITE | POSIX_GROUP_WRITE | POSIX_OWNER_WRITE), 0x0 });
        assertThat(externalFileAttributes.getData()[0] & 0xFF).isEqualTo(WINDOWS_ARCHIVE);
    }

    public void shouldApplyPathWhenWindows() throws IOException {
        Path path = mock(Path.class);
        FileSystem fileSystem = mock(FileSystem.class);
        FileSystemProvider provider = mock(FileSystemProvider.class);
        DosFileAttributeView dos = mock(DosFileAttributeView.class);

        when(path.getFileSystem()).thenReturn(fileSystem);
        when(fileSystem.provider()).thenReturn(provider);
        when(provider.getFileAttributeView(same(path), same(DosFileAttributeView.class), any())).thenReturn(dos);

        ExternalFileAttributes externalFileAttributes = ExternalFileAttributes.build(() -> WIN);

        externalFileAttributes.readFrom(new byte[] { WINDOWS_READ_ONLY, 0x0, 0x0, 0x0 });
        externalFileAttributes.apply(path);
        verify(dos, times(1)).setReadOnly(eq(true));
        reset(dos);

        externalFileAttributes.readFrom(new byte[] { WINDOWS_HIDDEN, 0x0, 0x0, 0x0 });
        externalFileAttributes.apply(path);
        verify(dos, times(1)).setHidden(eq(true));
        reset(dos);

        externalFileAttributes.readFrom(new byte[] { WINDOWS_SYSTEM, 0x0, 0x0, 0x0 });
        externalFileAttributes.apply(path);
        verify(dos, times(1)).setSystem(eq(true));
        reset(dos);

        externalFileAttributes.readFrom(new byte[] { WINDOWS_ARCHIVE, 0x0, 0x0, 0x0 });
        externalFileAttributes.apply(path);
        verify(dos, times(1)).setArchive(eq(true));
        reset(dos);
    }

    public void shouldApplyPathWhenPosix() throws IOException {
        Path path = mock(Path.class);
        FileSystem fileSystem = mock(FileSystem.class);
        FileSystemProvider provider = mock(FileSystemProvider.class);
        PosixFileAttributeView fileAttributeView = mock(PosixFileAttributeView.class);
        BasicFileAttributes basicFileAttributes = mock(BasicFileAttributes.class);
        PosixFileAttributes attributes = mock(PosixFileAttributes.class);

        when(path.getFileSystem()).thenReturn(fileSystem);
        when(fileSystem.provider()).thenReturn(provider);
        when(provider.getFileAttributeView(same(path), same(PosixFileAttributeView.class), any())).thenReturn(fileAttributeView);
        when(provider.readAttributes(same(path), same(BasicFileAttributes.class), any())).thenReturn(basicFileAttributes);
        when(fileAttributeView.readAttributes()).thenReturn(attributes);

        ExternalFileAttributes externalFileAttributes = ExternalFileAttributes.build(() -> MAC);

        Class<Set<PosixFilePermission>> listClass = (Class<Set<PosixFilePermission>>)(Class<?>)Set.class;
        ArgumentCaptor<Set<PosixFilePermission>> captor = ArgumentCaptor.forClass(listClass);

        externalFileAttributes.readFrom(new byte[] { 0x0, 0x0, POSIX_OTHERS_EXECUTE, 0x0 });
        externalFileAttributes.apply(path);
        verify(fileAttributeView).setPermissions(captor.capture());
        assertThat(captor.getValue()).containsExactly(OTHERS_EXECUTE);
        reset(fileAttributeView);

        externalFileAttributes.readFrom(new byte[] { 0x0, 0x0, POSIX_OTHERS_WRITE, 0x0 });
        externalFileAttributes.apply(path);
        verify(fileAttributeView).setPermissions(captor.capture());
        assertThat(captor.getValue()).containsExactly(OTHERS_WRITE);
        reset(fileAttributeView);

        externalFileAttributes.readFrom(new byte[] { 0x0, 0x0, POSIX_OTHERS_READ, 0x0 });
        externalFileAttributes.apply(path);
        verify(fileAttributeView).setPermissions(captor.capture());
        assertThat(captor.getValue()).containsExactly(OTHERS_READ);
        reset(fileAttributeView);

        externalFileAttributes.readFrom(new byte[] { 0x0, 0x0, POSIX_GROUP_EXECUTE, 0x0 });
        externalFileAttributes.apply(path);
        verify(fileAttributeView).setPermissions(captor.capture());
        assertThat(captor.getValue()).containsExactly(GROUP_EXECUTE);
        reset(fileAttributeView);

        externalFileAttributes.readFrom(new byte[] { 0x0, 0x0, POSIX_GROUP_WRITE, 0x0 });
        externalFileAttributes.apply(path);
        verify(fileAttributeView).setPermissions(captor.capture());
        assertThat(captor.getValue()).containsExactly(GROUP_WRITE);
        reset(fileAttributeView);

        externalFileAttributes.readFrom(new byte[] { 0x0, 0x0, POSIX_GROUP_READ, 0x0 });
        externalFileAttributes.apply(path);
        verify(fileAttributeView).setPermissions(captor.capture());
        assertThat(captor.getValue()).containsExactly(GROUP_READ);
        reset(fileAttributeView);

        externalFileAttributes.readFrom(new byte[] { 0x0, 0x0, POSIX_OWNER_EXECUTE, 0x0 });
        externalFileAttributes.apply(path);
        verify(fileAttributeView).setPermissions(captor.capture());
        assertThat(captor.getValue()).containsExactly(OWNER_EXECUTE);
        reset(fileAttributeView);

        externalFileAttributes.readFrom(new byte[] { 0x0, 0x0, (byte)POSIX_OWNER_WRITE, 0x0 });
        externalFileAttributes.apply(path);
        verify(fileAttributeView).setPermissions(captor.capture());
        assertThat(captor.getValue()).containsExactly(OWNER_WRITE);
        reset(fileAttributeView);

        externalFileAttributes.readFrom(new byte[] { 0x0, 0x0, 0x0, POSIX_OWNER_READ });
        externalFileAttributes.apply(path);
        verify(fileAttributeView).setPermissions(captor.capture());
        assertThat(captor.getValue()).containsExactlyInAnyOrder(OWNER_READ);
        reset(fileAttributeView);
    }

    public void shouldUseDefaultPermissionsForPosixWhenFileWasCreatedUnderWindows() throws IOException {
        Path path = mock(Path.class);
        FileSystem fileSystem = mock(FileSystem.class);
        FileSystemProvider provider = mock(FileSystemProvider.class);
        PosixFileAttributeView fileAttributeView = mock(PosixFileAttributeView.class);
        BasicFileAttributes basicFileAttributes = mock(BasicFileAttributes.class);
        PosixFileAttributes attributes = mock(PosixFileAttributes.class);

        when(path.getFileSystem()).thenReturn(fileSystem);
        when(fileSystem.provider()).thenReturn(provider);
        when(provider.getFileAttributeView(same(path), same(PosixFileAttributeView.class), any())).thenReturn(fileAttributeView);
        when(provider.readAttributes(same(path), same(BasicFileAttributes.class), any())).thenReturn(basicFileAttributes);
        when(fileAttributeView.readAttributes()).thenReturn(attributes);

        ExternalFileAttributes externalFileAttributes = ExternalFileAttributes.build(() -> MAC);

        Class<Set<PosixFilePermission>> listClass = (Class<Set<PosixFilePermission>>)(Class<?>)Set.class;
        ArgumentCaptor<Set<PosixFilePermission>> captor = ArgumentCaptor.forClass(listClass);

        assertThat(externalFileAttributes.readFrom(new byte[] { WINDOWS_READ_ONLY, 0x0, 0x0, 0x0 }).getDetails()).isEqualTo("?---------");

        externalFileAttributes.readFrom(new byte[] { 0x0, 0x0, 0x0, 0x0 }).apply(path);
        verify(fileAttributeView).setPermissions(captor.capture());
        assertThat(captor.getValue()).containsExactlyInAnyOrder(OWNER_READ, OWNER_WRITE, GROUP_READ, OTHERS_READ);
        reset(fileAttributeView);

        externalFileAttributes.readFrom(new byte[] { WINDOWS_READ_ONLY, 0x0, 0x0, 0x0 }).apply(path);
        verify(fileAttributeView).setPermissions(captor.capture());
        assertThat(captor.getValue()).containsExactlyInAnyOrder(OWNER_READ, GROUP_READ, OTHERS_READ);
        reset(fileAttributeView);
    }

    public void shouldUseDefaultPermissionsForWindowsWhenFileWasCreatedUnderPosix() throws IOException {
        ExternalFileAttributes externalFileAttributes = ExternalFileAttributes.build(() -> WIN);
        assertThat(externalFileAttributes.readFrom(new byte[] { 0x0, 0x0, 0x0, POSIX_OWNER_READ }).getDetails()).isEqualTo("none");

        Path path = mock(Path.class);
        FileSystem fileSystem = mock(FileSystem.class);
        FileSystemProvider provider = mock(FileSystemProvider.class);
        DosFileAttributeView dos = mock(DosFileAttributeView.class);

        when(path.getFileSystem()).thenReturn(fileSystem);
        when(fileSystem.provider()).thenReturn(provider);
        when(provider.getFileAttributeView(same(path), same(DosFileAttributeView.class), any())).thenReturn(dos);

        externalFileAttributes.apply(path);

        verify(dos, times(1)).setReadOnly(eq(true));
        verify(dos, times(1)).setHidden(eq(false));
        verify(dos, times(1)).setSystem(eq(false));
        verify(dos, times(1)).setArchive(eq(true));
    }

    public void shouldRetrieveDetailsNoneWhenUnknownFileSystem() {
        assertThat(ExternalFileAttributes.NULL.getDetails()).isEqualTo("none");
    }

    public void shouldRetrieveDetailsWhenWindowsFileSystem() {
        ExternalFileAttributes attributes = ExternalFileAttributes.build(() -> WIN);

        assertThat(attributes.getDetails()).isEqualTo("none");
        assertThat(attributes.readFrom(new byte[] { WINDOWS_READ_ONLY, 0x0, 0x0, 0x0 }).getDetails()).isEqualTo("read-only");
        assertThat(attributes.readFrom(new byte[] { WINDOWS_HIDDEN, 0x0, 0x0, 0x0 }).getDetails()).isEqualTo("hid");
        assertThat(attributes.readFrom(new byte[] { WINDOWS_SYSTEM, 0x0, 0x0, 0x0 }).getDetails()).isEqualTo("sys");
        assertThat(attributes.readFrom(new byte[] { WINDOWS_LABORATORY, 0x0, 0x0, 0x0 }).getDetails()).isEqualTo("lab");
        assertThat(attributes.readFrom(new byte[] { WINDOWS_DIRECTORY, 0x0, 0x0, 0x0 }).getDetails()).isEqualTo("dir");
        assertThat(attributes.readFrom(new byte[] { WINDOWS_ARCHIVE, 0x0, 0x0, 0x0 }).getDetails()).isEqualTo("arc");
        assertThat(attributes.readFrom(new byte[] { WINDOWS_READ_ONLY | WINDOWS_SYSTEM, 0x0, 0x0, 0x0 }).getDetails()).isEqualTo("rdo sys");
    }

    public void shouldRetrieveDetailsWhenPosixFileSystem() {
        ExternalFileAttributes attributes = ExternalFileAttributes.build(() -> MAC);

        assertThat(attributes.getDetails()).isEqualTo("?---------");
        assertThat(attributes.readFrom(new byte[] { 0x0, 0x0, POSIX_OTHERS_EXECUTE, 0x0 }).getDetails()).isEqualTo("?--------x");
        assertThat(attributes.readFrom(new byte[] { 0x0, 0x0, POSIX_OTHERS_EXECUTE | POSIX_OTHERS_WRITE, 0x0 }).getDetails()).isEqualTo("?-------wx");
        assertThat(attributes.readFrom(new byte[] { 0x0, 0x0,
                POSIX_OTHERS_EXECUTE | POSIX_OTHERS_WRITE | POSIX_OTHERS_READ, 0x0 }).getDetails()).isEqualTo("?------rwx");
        assertThat(attributes.readFrom(new byte[] { 0x0, 0x0,
                POSIX_OTHERS_EXECUTE | POSIX_OTHERS_WRITE | POSIX_OTHERS_READ
                        | POSIX_GROUP_EXECUTE, 0x0 }).getDetails()).isEqualTo("?-----xrwx");
        assertThat(attributes.readFrom(new byte[] { 0x0, 0x0,
                POSIX_OTHERS_EXECUTE | POSIX_OTHERS_WRITE | POSIX_OTHERS_READ
                        | POSIX_GROUP_EXECUTE | POSIX_GROUP_WRITE, 0x0 }).getDetails()).isEqualTo("?----wxrwx");
        assertThat(attributes.readFrom(new byte[] { 0x0, 0x0,
                POSIX_OTHERS_EXECUTE | POSIX_OTHERS_WRITE | POSIX_OTHERS_READ
                        | POSIX_GROUP_EXECUTE | POSIX_GROUP_WRITE | POSIX_GROUP_READ, 0x0 }).getDetails()).isEqualTo("?---rwxrwx");
        assertThat(attributes.readFrom(new byte[] { 0x0, 0x0,
                POSIX_OTHERS_EXECUTE | POSIX_OTHERS_WRITE | POSIX_OTHERS_READ
                        | POSIX_GROUP_EXECUTE | POSIX_GROUP_WRITE | POSIX_GROUP_READ
                        | POSIX_OWNER_EXECUTE, 0x0 }).getDetails()).isEqualTo("?--xrwxrwx");
        assertThat(attributes.readFrom(new byte[] { 0x0, 0x0,
                (byte)(POSIX_OTHERS_EXECUTE | POSIX_OTHERS_WRITE | POSIX_OTHERS_READ
                        | POSIX_GROUP_EXECUTE | POSIX_GROUP_WRITE | POSIX_GROUP_READ
                        | POSIX_OWNER_EXECUTE | POSIX_OWNER_WRITE), 0x0 }).getDetails()).isEqualTo("?-wxrwxrwx");
        assertThat(attributes.readFrom(new byte[] { 0x0, 0x0,
                (byte)(POSIX_OTHERS_EXECUTE | POSIX_OTHERS_WRITE | POSIX_OTHERS_READ
                        | POSIX_GROUP_EXECUTE | POSIX_GROUP_WRITE | POSIX_GROUP_READ
                        | POSIX_OWNER_EXECUTE | POSIX_OWNER_WRITE),
                POSIX_OWNER_READ }).getDetails()).isEqualTo("?rwxrwxrwx");
        assertThat(attributes.readFrom(new byte[] { 0x0, 0x0,
                (byte)(POSIX_OTHERS_EXECUTE | POSIX_OTHERS_WRITE | POSIX_OTHERS_READ
                        | POSIX_GROUP_EXECUTE | POSIX_GROUP_WRITE | POSIX_GROUP_READ
                        | POSIX_OWNER_EXECUTE | POSIX_OWNER_WRITE),
                POSIX_OWNER_READ | POSIX_DIRECTORY }).getDetails()).isEqualTo("drwxrwxrwx");
        assertThat(attributes.readFrom(new byte[] { 0x0, 0x0,
                (byte)(POSIX_OTHERS_EXECUTE | POSIX_OTHERS_WRITE | POSIX_OTHERS_READ
                        | POSIX_GROUP_EXECUTE | POSIX_GROUP_WRITE | POSIX_GROUP_READ
                        | POSIX_OWNER_EXECUTE | POSIX_OWNER_WRITE),
                (byte)(POSIX_OWNER_READ | POSIX_REGULAR_FILE) }).getDetails()).isEqualTo("-rwxrwxrwx");
    }

}

