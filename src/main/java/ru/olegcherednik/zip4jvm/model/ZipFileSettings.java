package ru.olegcherednik.zip4jvm.model;

import lombok.Builder;
import lombok.Getter;

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

    private final ZipEntrySettings defZipEntrySettings;

}
