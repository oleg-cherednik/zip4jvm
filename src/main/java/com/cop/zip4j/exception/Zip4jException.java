package com.cop.zip4j.exception;

import lombok.Getter;

@Getter
public class Zip4jException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private ErrorCode code = ErrorCode.UNKNOWN;

    public Zip4jException() {
    }

    public Zip4jException(String msg) {
        super(msg);
    }

    public Zip4jException(String message, Throwable cause) {
        super(message, cause);
    }

    public Zip4jException(String msg, ErrorCode code) {
        super(msg);
        this.code = code;
    }

    public Zip4jException(String message, Throwable cause, ErrorCode code) {
        super(message, cause);
        this.code = code;
    }

    public Zip4jException(Throwable cause) {
        super(cause);
    }

    public Zip4jException(Throwable cause, ErrorCode code) {
        super(cause);
        this.code = code;
    }

}
