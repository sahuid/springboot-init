package com.sahuid.springbootinit.exception;

public class NoLoginException extends RuntimeException{

    public NoLoginException(String message) {
        super(message);
    }
}
