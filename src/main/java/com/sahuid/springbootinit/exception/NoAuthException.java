package com.sahuid.springbootinit.exception;

public class NoAuthException extends RuntimeException{
    public NoAuthException(String message) {
        super(message);
    }
}
