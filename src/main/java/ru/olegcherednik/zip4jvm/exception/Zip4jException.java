package ru.olegcherednik.zip4jvm.exception;

import lombok.Getter;

@Getter
public class Zip4jException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final ErrorCode code;

    public Zip4jException() {
        this(null, ErrorCode.UNKNOWN);
    }

    public Zip4jException(String message) {
        this(message, ErrorCode.UNKNOWN);
    }

    public Zip4jException(String message, Throwable cause) {
        this(message, ErrorCode.UNKNOWN, cause);
    }

    public Zip4jException(String message, ErrorCode code) {
        this(message, code, null);
    }

    public Zip4jException(Throwable cause) {
        this(ErrorCode.UNKNOWN, cause);
    }

    public Zip4jException(ErrorCode code, Throwable cause) {
        this(cause.getMessage(), code, cause);
    }

    public Zip4jException(String message, ErrorCode code, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

}
