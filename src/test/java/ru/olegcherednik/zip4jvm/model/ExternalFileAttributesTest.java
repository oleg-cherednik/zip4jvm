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
        assertThat(externalFileAttributes.get()[0] & 0xFF).isEqualTo(BIT0);
        reset(dos);

        when(dos.isHidden()).thenReturn(true);
        externalFileAttributes.readFrom(path);
        assertThat(externalFileAttributes.get()[0] & 0xFF).isEqualTo(BIT1);
        reset(dos);

        when(dos.isSystem()).thenReturn(true);
        externalFileAttributes.readFrom(path);
        assertThat(externalFileAttributes.get()[0] & 0xFF).isEqualTo(BIT2);
        reset(dos);

        when(dos.isArchive()).thenReturn(true);
        externalFileAttributes.readFrom(path);
        assertThat(externalFileAttributes.get()[0] & 0xFF).isEqualTo(BIT5);
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
        assertThat(externalFileAttributes.get()[2] & 0xFF).isEqualTo(BIT0);
        reset(attributes);

        when(attributes.permissions()).thenReturn(Collections.singleton(OTHERS_WRITE));
        externalFileAttributes.readFrom(path);
        assertThat(externalFileAttributes.get()[2] & 0xFF).isEqualTo(BIT1);
        reset(attributes);

        when(attributes.permissions()).thenReturn(Collections.singleton(OTHERS_READ));
        externalFileAttributes.readFrom(path);
        assertThat(externalFileAttributes.get()[2] & 0xFF).isEqualTo(BIT2);
        reset(attributes);

        when(attributes.permissions()).thenReturn(Collections.singleton(GROUP_EXECUTE));
        externalFileAttributes.readFrom(path);
        assertThat(externalFileAttributes.get()[2] & 0xFF).isEqualTo(BIT3);
        reset(attributes);

        when(attributes.permissions()).thenReturn(Collections.singleton(GROUP_WRITE));
        externalFileAttributes.readFrom(path);
        assertThat(externalFileAttributes.get()[2] & 0xFF).isEqualTo(BIT4);
        reset(attributes);

        when(attributes.permissions()).thenReturn(Collections.singleton(GROUP_READ));
        externalFileAttributes.readFrom(path);
        assertThat(externalFileAttributes.get()[2] & 0xFF).isEqualTo(BIT5);
        reset(attributes);

        when(attributes.permissions()).thenReturn(Collections.singleton(OWNER_EXECUTE));
        externalFileAttributes.readFrom(path);
        assertThat(externalFileAttributes.get()[2] & 0xFF).isEqualTo(BIT6);
        reset(attributes);

        when(attributes.permissions()).thenReturn(Collections.singleton(OWNER_WRITE));
        externalFileAttributes.readFrom(path);
        assertThat(externalFileAttributes.get()[2] & 0xFF).isEqualTo(BIT7);
        reset(attributes);

        when(attributes.permissions()).thenReturn(Collections.singleton(OWNER_READ));
        externalFileAttributes.readFrom(path);
        assertThat(externalFileAttributes.get()[3] & 0xFF).isEqualTo(BIT0);
        reset(attributes);

        when(basicFileAttributes.isDirectory()).thenReturn(true);
        externalFileAttributes.readFrom(path);
        assertThat(externalFileAttributes.get()[3] & 0xFF).isEqualTo(BIT6);
        reset(basicFileAttributes);

        when(basicFileAttributes.isRegularFile()).thenReturn(true);
        externalFileAttributes.readFrom(path);
        assertThat(externalFileAttributes.get()[3] & 0xFF).isEqualTo(BIT7);
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

    public void shouldRetrievePosixInstanceWhenMacOrUnixSystem1() throws IOException {
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
        assertThat(externalFileAttributes.get()).isEqualTo(new byte[] { 0x0, 0x0, 0x0, 0x0 });
    }

    public void shouldIgnoreReadFromPathNullObject() throws IOException {
        ExternalFileAttributes externalFileAttributes = ExternalFileAttributes.NULL;
        externalFileAttributes.readFrom(fileBentley);
        assertThat(externalFileAttributes.get()).isEqualTo(new byte[] { 0x0, 0x0, 0x0, 0x0 });
    }

    public void shouldIgnoreApplyPathWhenNullObject() throws IOException {
        Path path = mock(Path.class);
        ExternalFileAttributes.NULL.apply(fileBentley);
        verify(path, never()).getFileSystem();
    }

    public void shouldReadFromByteArrayWenWindows() {
        ExternalFileAttributes externalFileAttributes = ExternalFileAttributes.build(() -> WIN);

        for (int b0 : Arrays.asList(BIT0, BIT1, BIT2, BIT5)) {
            externalFileAttributes.readFrom(new byte[] { (byte)b0, 0x0, 0x0, 0x0 });
            assertThat(externalFileAttributes.get()[0] & 0xFF).isEqualTo(b0);
        }

        externalFileAttributes.readFrom(new byte[] { 0x0, 0x0, (byte)BitUtils.clearBits(0xFF, BIT1 | BIT4 | BIT7), 0x0 });
        assertThat(externalFileAttributes.get()[0] & 0xFF).isEqualTo(BIT0 | BIT5);
    }

    public void shouldReadFromByteArrayWenPosix() {
        ExternalFileAttributes externalFileAttributes = ExternalFileAttributes.build(() -> MAC);

        for (int b2 : Arrays.asList(BIT0, BIT1, BIT2, BIT3, BIT4, BIT5, BIT6, BIT7)) {
            externalFileAttributes.readFrom(new byte[] { 0x0, 0x0, (byte)b2, 0x0 });
            assertThat(externalFileAttributes.get()[2] & 0xFF).isEqualTo(b2);
            assertThat(externalFileAttributes.get()[3] & 0xFF).isEqualTo(0x0);
        }

        externalFileAttributes.readFrom(new byte[] { BIT0, 0x0, 0x0, 0x0 });
        assertThat(externalFileAttributes.get()[2] & 0xFF).isEqualTo(BIT2 | BIT5);
        assertThat(externalFileAttributes.get()[3] & 0xFF).isEqualTo(BIT0 | BIT7);

        externalFileAttributes.readFrom(new byte[] { (byte)BitUtils.clearBits(0xFF, BIT0), 0x0, 0x0, 0x0 });
        assertThat(externalFileAttributes.get()[2] & 0xFF).isEqualTo(BIT2 | BIT5 | BIT7);
        assertThat(externalFileAttributes.get()[3] & 0xFF).isEqualTo(BIT0 | BIT7);

        externalFileAttributes.readFrom(new byte[] { 0x0, 0x0, 0x0, BIT6 });
        assertThat(externalFileAttributes.get()[2] & 0xFF).isEqualTo(0x0);
        assertThat(externalFileAttributes.get()[3] & 0xFF).isEqualTo(BIT6);

        externalFileAttributes.readFrom(new byte[] { 0x0, 0x0, 0x0, (byte)BIT7 });
        assertThat(externalFileAttributes.get()[2] & 0xFF).isEqualTo(0x0);
        assertThat(externalFileAttributes.get()[3] & 0xFF).isEqualTo(BIT7);
    }

    public void shouldRestoreDefaultWhenReadFromEmptyByteArray() {
        ExternalFileAttributes externalFileAttributes = ExternalFileAttributes.build(() -> WIN);
        externalFileAttributes.readFrom(new byte[] { 0x0, 0x0, 0x0, 0x0 });
        assertThat(externalFileAttributes.get()[0] & 0xFF).isEqualTo(BIT5);

        externalFileAttributes = ExternalFileAttributes.build(() -> MAC);
        externalFileAttributes.readFrom(new byte[] { 0x0, 0x0, 0x0, 0x0 });
        assertThat(externalFileAttributes.get()[2] & 0xFF).isEqualTo(BIT2 | BIT5 | BIT7);
        assertThat(externalFileAttributes.get()[3] & 0xFF).isEqualTo(BIT0 | BIT7);
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

        externalFileAttributes.readFrom(new byte[] { BIT0, 0x0, 0x0, 0x0 });
        externalFileAttributes.apply(path);
        verify(dos, times(1)).setReadOnly(eq(true));
        reset(dos);

        externalFileAttributes.readFrom(new byte[] { BIT1, 0x0, 0x0, 0x0 });
        externalFileAttributes.apply(path);
        verify(dos, times(1)).setHidden(eq(true));
        reset(dos);

        externalFileAttributes.readFrom(new byte[] { BIT2, 0x0, 0x0, 0x0 });
        externalFileAttributes.apply(path);
        verify(dos, times(1)).setSystem(eq(true));
        reset(dos);

        externalFileAttributes.readFrom(new byte[] { BIT5, 0x0, 0x0, 0x0 });
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

        externalFileAttributes.readFrom(new byte[] { 0x0, 0x0, BIT0, 0x0 });
        externalFileAttributes.apply(path);
        verify(fileAttributeView).setPermissions(captor.capture());
        assertThat(captor.getValue()).containsExactly(OTHERS_EXECUTE);
        reset(fileAttributeView);

        externalFileAttributes.readFrom(new byte[] { 0x0, 0x0, BIT1, 0x0 });
        externalFileAttributes.apply(path);
        verify(fileAttributeView).setPermissions(captor.capture());
        assertThat(captor.getValue()).containsExactly(OTHERS_WRITE);
        reset(fileAttributeView);

        externalFileAttributes.readFrom(new byte[] { 0x0, 0x0, BIT2, 0x0 });
        externalFileAttributes.apply(path);
        verify(fileAttributeView).setPermissions(captor.capture());
        assertThat(captor.getValue()).containsExactly(OTHERS_READ);
        reset(fileAttributeView);

        externalFileAttributes.readFrom(new byte[] { 0x0, 0x0, BIT3, 0x0 });
        externalFileAttributes.apply(path);
        verify(fileAttributeView).setPermissions(captor.capture());
        assertThat(captor.getValue()).containsExactly(GROUP_EXECUTE);
        reset(fileAttributeView);

        externalFileAttributes.readFrom(new byte[] { 0x0, 0x0, BIT4, 0x0 });
        externalFileAttributes.apply(path);
        verify(fileAttributeView).setPermissions(captor.capture());
        assertThat(captor.getValue()).containsExactly(GROUP_WRITE);
        reset(fileAttributeView);

        externalFileAttributes.readFrom(new byte[] { 0x0, 0x0, BIT5, 0x0 });
        externalFileAttributes.apply(path);
        verify(fileAttributeView).setPermissions(captor.capture());
        assertThat(captor.getValue()).containsExactly(GROUP_READ);
        reset(fileAttributeView);

        externalFileAttributes.readFrom(new byte[] { 0x0, 0x0, BIT6, 0x0 });
        externalFileAttributes.apply(path);
        verify(fileAttributeView).setPermissions(captor.capture());
        assertThat(captor.getValue()).containsExactly(OWNER_EXECUTE);
        reset(fileAttributeView);

        externalFileAttributes.readFrom(new byte[] { 0x0, 0x0, (byte)BIT7, 0x0 });
        externalFileAttributes.apply(path);
        verify(fileAttributeView).setPermissions(captor.capture());
        assertThat(captor.getValue()).containsExactly(OWNER_WRITE);
        reset(fileAttributeView);

        externalFileAttributes.readFrom(new byte[] { 0x0, 0x0, 0x0, BIT0 });
        externalFileAttributes.apply(path);
        verify(fileAttributeView).setPermissions(captor.capture());
        assertThat(captor.getValue()).containsExactly(OWNER_READ);
        reset(fileAttributeView);
    }

}

