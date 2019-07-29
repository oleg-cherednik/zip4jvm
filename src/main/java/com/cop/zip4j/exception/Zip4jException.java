package com.cop.zip4j.exception;

public class Zip4jException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private int code = -1;

    public Zip4jException() {
    }

    public Zip4jException(String msg) {
        super(msg);
    }

    public Zip4jException(String message, Throwable cause) {
        super(message, cause);
    }

    public Zip4jException(String msg, int code) {
        super(msg);
        this.code = code;
    }

    public Zip4jException(String message, Throwable cause, int code) {
        super(message, cause);
        this.code = code;
    }

    public Zip4jException(Throwable cause) {
        super(cause);
    }

    public Zip4jException(Throwable cause, int code) {
        super(cause);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

}
