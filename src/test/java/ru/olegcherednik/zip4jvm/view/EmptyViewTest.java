package ru.olegcherednik.zip4jvm.view;

import org.testng.annotations.Test;

import java.io.PrintStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

/**
 * @author Oleg Cherednik
 * @since 29.12.2019
 */
@Test
public class EmptyViewTest {

    public void shouldRetrieveFalseWhenPrint() {
        PrintStream out = mock(PrintStream.class);
        assertThat(EmptyView.INSTANCE.print(out)).isFalse();
        assertThat(EmptyView.INSTANCE.print(out, false)).isFalse();
        assertThat(EmptyView.INSTANCE.print(out, true)).isFalse();
        verifyZeroInteractions(out);
    }

}
