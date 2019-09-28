package ru.olegcherednik.zip4jvm.model;

import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.DosFileAttributes;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.spi.FileSystemProvider;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static ru.olegcherednik.zip4jvm.model.ExternalFileAttributes.MAC;
import static ru.olegcherednik.zip4jvm.model.ExternalFileAttributes.UNIX;
import static ru.olegcherednik.zip4jvm.model.ExternalFileAttributes.WIN;

/**
 * @author Oleg Cherednik
 * @since 23.09.2019
 */
@Test
public class ExternalFileAttributesTest {

    public void shouldRetrieveWindowsInstanceWhenWindowsSystem() throws IOException {
        Path path = mock(Path.class);
        FileSystem fileSystem = mock(FileSystem.class);
        FileSystemProvider provider = mock(FileSystemProvider.class);
        DosFileAttributeView fileAttributeView = mock(DosFileAttributeView.class);
        BasicFileAttributes basicFileAttributes = mock(BasicFileAttributes.class);
        DosFileAttributes attributes = mock(DosFileAttributes.class);

        when(path.getFileSystem()).thenReturn(fileSystem);
        when(fileSystem.provider()).thenReturn(provider);
        when(provider.getFileAttributeView(same(path), same(DosFileAttributeView.class), any())).thenReturn(fileAttributeView);
        when(provider.readAttributes(same(path), same(BasicFileAttributes.class), any())).thenReturn(basicFileAttributes);
        when(fileAttributeView.readAttributes()).thenReturn(attributes);
        when(basicFileAttributes.isDirectory()).thenReturn(false);
        when(basicFileAttributes.isRegularFile()).thenReturn(true);

        ExternalFileAttributes externalFileAttributes = ExternalFileAttributes.createOperationBasedDelegate(path, () -> WIN);
        assertThat(externalFileAttributes.toString()).isEqualTo("win");
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
            ExternalFileAttributes externalFileAttributes = ExternalFileAttributes.createOperationBasedDelegate(path, () -> osName);
            assertThat(externalFileAttributes.toString()).isEqualTo("posix");
        }
    }

    public void shouldRetrieveUnknownInstanceWhenUnknownSystem() throws IOException {
        Path path = mock(Path.class);

        ExternalFileAttributes externalFileAttributes = ExternalFileAttributes.createOperationBasedDelegate(path, () -> "<unknown>");
        assertThat(externalFileAttributes.toString()).isEqualTo("<null>");
    }

    public void shouldRetrievePosixImplementationWhen2And3BytesNotZero() {
        for (byte[] data : Arrays.asList(new byte[] { 0x0, 0x0, 0xA, 0x0 }, new byte[] { 0x0, 0x0, 0x0, 0xA }))
            assertThat(ExternalFileAttributes.createDataBasedDelegate(data).toString()).isEqualTo("posix");
    }

    public void shouldRetrieveWindowsPosixImplementationWhen0byteNotZero() {
        ExternalFileAttributes externalFileAttributes = ExternalFileAttributes.createDataBasedDelegate(new byte[] { 0xA, 0x0, 0x0, 0x0 });
        assertThat(externalFileAttributes.toString()).isEqualTo("win");
    }

    public void shouldRetrieveWindowsPosixImplementationWhenAllBytesZero() {
        ExternalFileAttributes externalFileAttributes = ExternalFileAttributes.createDataBasedDelegate(new byte[] { 0x0, 0x0, 0x0, 0x0 });
        assertThat(externalFileAttributes.toString()).isEqualTo("<null>");
    }

}
