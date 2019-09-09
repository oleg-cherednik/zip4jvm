package ru.olegcherednik.zip4jvm.model.settings;

import lombok.Builder;
import lombok.Getter;

/**
 * @author Oleg Cherednik
 * @since 05.09.2019
 */
@Getter
@Builder
public final class ZipFileWriterSettings {

    private final long splitSize;
    private final String comment;
    private final boolean zip64;

    private final ZipEntrySettings entrySettings;

}
