package ru.olegcherednik.zip4jvm.exception;

import lombok.Getter;
import ru.olegcherednik.zip4jvm.view.ViewUtils;

/**
 * @author Oleg Cherednik
 * @since 04.12.2019
 */
@Getter
public class SignatureWasNotFoundException extends Zip4jvmException {

    private static final long serialVersionUID = -8774784649137793410L;

    private final int signature;
    private final String name;
    private final long offs;

    public SignatureWasNotFoundException(int signature, String name) {
        super(String.format("Signature %s (%s) was not found", ViewUtils.signature(signature), name));
        this.signature = signature;
        this.name = name;
        offs = -1;
    }

    public SignatureWasNotFoundException(int signature, String name, long offs) {
        super(String.format("Signature %s (%s) was not found: (0x%08X)", ViewUtils.signature(signature), name, offs));
        this.signature = signature;
        this.name = name;
        this.offs = offs;
    }

}
