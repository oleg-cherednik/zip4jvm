package ru.olegcherednik.zip4jvm.model;

import org.testng.annotations.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * @author Oleg Cherednik
 * @since 15.09.2019
 */
@Test
public class DigitalSignatureTest {

    public void shouldUseSettersGettersCorrectly() {
        CentralDirectory.DigitalSignature digitalSignature = new CentralDirectory.DigitalSignature();
        assertThat(digitalSignature.getSignatureData()).isNull();

        byte[] signatureData = { 1, 2 };
        digitalSignature.setSignatureData(signatureData);
        assertThat(digitalSignature.getSignatureData()).isSameAs(signatureData);
    }

}
