package io.databaton.exception;

public class SerializeException extends RuntimeException{

    public SerializeException(){}

    public SerializeException(String message){
        super(message);
    }
}
