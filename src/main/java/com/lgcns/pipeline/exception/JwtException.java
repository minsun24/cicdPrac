package com.lgcns.pipeline.exception;


public class JwtException extends RuntimeException {
    public JwtException(String msg) {
        super("JwtException: " + msg);
    }
}
