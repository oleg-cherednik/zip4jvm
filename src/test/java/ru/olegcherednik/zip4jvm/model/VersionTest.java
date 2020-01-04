package ru.olegcherednik.zip4jvm.model;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Oleg Cherednik
 * @since 02.01.2020
 */
@Test
public class VersionTest {

    public void shouldParseFileSystemFromCodeWhenCodeKnown() {
        for (Version.FileSystem fileSystem : Version.FileSystem.values())
            assertThat(Version.FileSystem.parseCode(fileSystem.getCode())).isSameAs(fileSystem);
    }

    public void shouldRetrieveUnknownWhenCodeUnknown() {
        assertThat(Version.FileSystem.parseCode(-1)).isSameAs(Version.FileSystem.UNKNOWN);
    }

    public void shouldRetrieveStringWhenNull() {
        assertThat(Version.NULL.toString()).isEqualTo("<null>");
    }

    public void shouldRetrieveStringWhenNotNull() {
        assertThat(Version.of(Version.FileSystem.NTFS, 20).toString()).isEqualTo("NTFS / 2.0");
    }

    public void shouldRetrieveNullWhenBuildWithNullFileSystem() {
        assertThat(Version.of(null, 20)).isSameAs(Version.NULL);
    }

    public void shouldRetrieveSeparateObjectWhenBuildWithKnownFileSystem() {
        Version one = Version.of(Version.FileSystem.AMIGA, 20);
        Version two = Version.of(Version.FileSystem.AMIGA, 20);
        assertThat(one).isNotSameAs(two);
        assertThat(one).isEqualTo(two);
    }

}
