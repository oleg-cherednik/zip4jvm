package ru.olegcherednik.zip4jvm.model;

import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Oleg Cherednik
 * @since 01.10.2019
 */
@Test
public class EncryptionTest {

    public void shouldThrowExceptionWhenStrongEncryptionFlatIsSet() {
        ExtraField.Record record = mock(ExtraField.Record.class);
        when(record.isNull()).thenReturn(false);
        when(record.getSignature()).thenReturn(666);

        ExtraField extraField = ExtraField.builder().addRecord(record).build();

        GeneralPurposeFlag generalPurposeFlag = new GeneralPurposeFlag();
        generalPurposeFlag.setStrongEncryption(true);

        assertThatThrownBy(() -> Encryption.get(extraField, generalPurposeFlag)).isExactlyInstanceOf(Zip4jvmException.class);
    }
}
