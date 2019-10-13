package ru.olegcherednik.zip4jvm.model;

import lombok.Builder;
import lombok.Getter;

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
}
