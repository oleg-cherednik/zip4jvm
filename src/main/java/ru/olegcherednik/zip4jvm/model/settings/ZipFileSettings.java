package ru.olegcherednik.zip4jvm.model.settings;

import lombok.Builder;
import lombok.Getter;

import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 05.09.2019
 */
@Getter
@Builder
public final class ZipFileSettings {

    private final long splitSize;
    private final String comment;
    private final boolean zip64;

    private final ZipEntrySettings defEntrySettings;

    private final Function<String, ZipEntrySettings> entrySettingsProvider;

    public Function<String, ZipEntrySettings> getEntrySettingsProvider() {
        return entrySettingsProvider == null ? fileName -> defEntrySettings : entrySettingsProvider;
    }


}
