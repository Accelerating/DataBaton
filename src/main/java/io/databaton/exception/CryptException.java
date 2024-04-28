package io.databaton.exception;

public class CryptException extends RuntimeException{

    public CryptException(){}

    public CryptException(String message){
        super(message);
    }

}
