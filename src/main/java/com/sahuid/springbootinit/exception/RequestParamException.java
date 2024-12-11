package com.sahuid.springbootinit.exception;

public class RequestParamException extends RuntimeException{
    public RequestParamException(String message) {
        super(message);
    }
}
