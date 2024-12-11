package com.sahuid.springbootinit.exception;

public class DataBaseAbsentException extends RuntimeException{

    public DataBaseAbsentException(String message) {
        super(message);
    }
}
