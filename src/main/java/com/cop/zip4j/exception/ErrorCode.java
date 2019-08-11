package com.cop.zip4j.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author Oleg Cherednik
 * @since 10.08.2019
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum ErrorCode {
    UNKNOWN(-1),
    INCORRECT_PASSWORD(1),
    EMPTY_PASSWORD(2),
    PATH_NOT_EXISTS(3);

    private final int code;


}
