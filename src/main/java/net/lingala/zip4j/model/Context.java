package net.lingala.zip4j.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import net.lingala.zip4j.util.Compression;

/**
 * @author Oleg Cherednik
 * @since 02.03.2019
 */
@Getter
@Builder
@AllArgsConstructor
public class Context {
    @Builder.Default
    private final Compression compression = Compression.STORE;
}
