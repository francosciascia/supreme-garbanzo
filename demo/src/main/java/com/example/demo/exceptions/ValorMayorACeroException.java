package com.example.demo.exceptions;

public class ValorMayorACeroException extends Throwable {

    public ValorMayorACeroException(){

    }

    public ValorMayorACeroException(String message) {
        super(message);
    }

    public ValorMayorACeroException(String message, Throwable cause) {
        super(message, cause);
    }

    public ValorMayorACeroException(Throwable cause) {
        super(cause);
    }

    public ValorMayorACeroException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}


