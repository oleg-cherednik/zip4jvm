package ru.olegcherednik.zip4jvm.model.settings;

import lombok.Builder;
import lombok.Getter;

import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 07.09.2019
 */
@Getter
@Builder
public final class ZipFileReadSettings {

    private final Function<String, char[]> password;

}
