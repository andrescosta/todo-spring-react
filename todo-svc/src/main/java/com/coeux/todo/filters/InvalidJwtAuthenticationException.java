package com.coeux.todo.filters;

public class InvalidJwtAuthenticationException extends Throwable{

    public InvalidJwtAuthenticationException() {
    }

    public InvalidJwtAuthenticationException(String message) {
        super(message);
    }

    public InvalidJwtAuthenticationException(Throwable cause) {
        super(cause);
    }

    public InvalidJwtAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    
}
