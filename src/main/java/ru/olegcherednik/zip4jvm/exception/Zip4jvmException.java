package ru.olegcherednik.zip4jvm.exception;

import lombok.Getter;

@Getter
public class Zip4jvmException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final ErrorCode code;

    public Zip4jvmException() {
        this(null, ErrorCode.UNKNOWN);
    }

    public Zip4jvmException(String message) {
        this(message, ErrorCode.UNKNOWN);
    }

    public Zip4jvmException(String message, Throwable cause) {
        this(message, ErrorCode.UNKNOWN, cause);
    }

    public Zip4jvmException(String message, ErrorCode code) {
        this(message, code, null);
    }

    public Zip4jvmException(Throwable cause) {
        this(ErrorCode.UNKNOWN, cause);
    }

    public Zip4jvmException(ErrorCode code, Throwable cause) {
        this(cause.getMessage(), code, cause);
    }

    public Zip4jvmException(String message, ErrorCode code, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

}
