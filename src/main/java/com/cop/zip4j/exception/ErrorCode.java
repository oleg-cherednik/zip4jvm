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
    ILLEGAL_PASSWORD(1);

    private final int code;


}
