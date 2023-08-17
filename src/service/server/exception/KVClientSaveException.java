package service.server.exception;

public class KVClientSaveException extends RuntimeException {

    public KVClientSaveException(String message, Exception exc) {
        super(message, exc);
    }

    public KVClientSaveException(String message) {
        super(message);
    }
}


