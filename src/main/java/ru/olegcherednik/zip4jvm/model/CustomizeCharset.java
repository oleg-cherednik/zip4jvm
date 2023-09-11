package ru.olegcherednik.zip4jvm.model;

import java.nio.charset.Charset;

/**
 * @author Oleg Cherednik
 * @since 11.09.2023
 */
public interface CustomizeCharset {

    Charset customize(Charset charset);
}
