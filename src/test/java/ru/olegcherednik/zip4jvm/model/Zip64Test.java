package ru.olegcherednik.zip4jvm.model;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Oleg Cherednik
 * @since 01.10.2019
 */
@Test
@SuppressWarnings("NewClassNamingConvention")
public class Zip64Test {

    public void shouldRetrieveNullObjectWhenOneOfArgumentsIsNull() {
        Zip64.EndCentralDirectoryLocator endCentralDirectoryLocator = new Zip64.EndCentralDirectoryLocator();
        Zip64.EndCentralDirectory endCentralDirectory = new Zip64.EndCentralDirectory();

        assertThat(Zip64.of(null, null)).isSameAs(Zip64.NULL);
        assertThat(Zip64.of(endCentralDirectoryLocator, null)).isSameAs(Zip64.NULL);
        assertThat(Zip64.of(null, endCentralDirectory)).isSameAs(Zip64.NULL);
        assertThat(Zip64.of(endCentralDirectoryLocator, endCentralDirectory)).isNotSameAs(Zip64.NULL);
    }

    public void shouldRetrieveSpecialStringForNullObjectWhenToString() {
        Zip64 zip64 = Zip64.of(new Zip64.EndCentralDirectoryLocator(), new Zip64.EndCentralDirectory());
        assertThat(zip64.toString()).isNotEqualTo("<null>");
        assertThat(Zip64.NULL.toString()).isEqualTo("<null>");
    }
}
