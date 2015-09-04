package com.otognan.driverpete.logic;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class ForbiddenDataException extends RuntimeException {
    public ForbiddenDataException(String message) {
        super(message);
    }
}