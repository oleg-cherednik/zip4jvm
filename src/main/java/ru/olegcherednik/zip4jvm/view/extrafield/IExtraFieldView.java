package ru.olegcherednik.zip4jvm.view.extrafield;

import ru.olegcherednik.zip4jvm.view.IView;

/**
 * @author Oleg Cherednik
 * @since 24.11.2019
 */
public interface IExtraFieldView extends IView {

    int getSignature();

    String getTitle();

    default String getFileName() {
        return String.format("(0x%04X)_%s", getSignature(), getTitle());
    }

}
