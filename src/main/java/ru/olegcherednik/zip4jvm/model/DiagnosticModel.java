package ru.olegcherednik.zip4jvm.model;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

/**
 * @author Oleg Cherednik
 * @since 12.10.2019
 */
@SuppressWarnings("FieldNamingConvention")
@Getter
@Builder
public final class DiagnosticModel {
    private final EndCentralDirectory endCentralDirectory;
    private final Zip64 zip64;
    private final CentralDirectory centralDirectory;

    private final long endCentralDirectoryOffs;
    private final long endCentralDirectorySize;

    private final long zip64EndCentralDirectoryLocatorOffs;
    private final long zip64EndCentralDirectoryLocatorSize;

    private final long zip64EndCentralDirectoryOffs;
    private final long zip64EndCentralDirectorySize;

    private final long centralDirectoryOffs;
    private final long centralDirectorySize;

    private final Map<String, Long> fileHeaderOffs;
    private final Map<String, Long> fileHeaderSize;
}
