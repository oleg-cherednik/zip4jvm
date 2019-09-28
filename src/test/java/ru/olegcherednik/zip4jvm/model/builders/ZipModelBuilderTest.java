package ru.olegcherednik.zip4jvm.model.builders;

import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.model.settings.ZipFileSettings;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ru.olegcherednik.zip4jvm.TestData.zipStoreSolid;

/**
 * @author Oleg Cherednik
 * @since 27.09.2019
 */
@Test
public class ZipModelBuilderTest {

    public void shouldThrowExceptionWhenCreateModelForExistedFile() {
        assertThatThrownBy(() -> ZipModelBuilder.create(zipStoreSolid, ZipFileSettings.DEFAULT)).isExactlyInstanceOf(Zip4jvmException.class);
    }

}
