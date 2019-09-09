package ru.olegcherednik.zip4jvm.model.settings;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 07.09.2019
 */
@Getter
@Builder
public final class ZipFileReaderSettings {

    @NonNull
    @Builder.Default
    private final Function<String, char[]> password = fileName -> null;

}
