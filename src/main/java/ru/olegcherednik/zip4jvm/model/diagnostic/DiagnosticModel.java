package ru.olegcherednik.zip4jvm.model.diagnostic;

import lombok.Builder;
import lombok.Getter;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.EndCentralDirectory;
import ru.olegcherednik.zip4jvm.model.Zip64;

/**
 * @author Oleg Cherednik
 * @since 12.10.2019
 */
@Getter
@Builder
public final class DiagnosticModel {

    private final EndCentralDirectory endCentralDirectory;
    private final Zip64 zip64;
    private final CentralDirectory centralDirectory;
    private final Diagnostic diagnostic;
}
