package ru.olegcherednik.zip4jvm.model.settings;

import lombok.Builder;
import lombok.Getter;

/**
 * @author Oleg Cherednik
 * @since 07.09.2019
 */
@Getter
@Builder
public final class ZipFileReadSettings {

    private final char[] password;

}
