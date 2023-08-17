package service.server.exception;

public class KVClientLoadException extends RuntimeException {

    public KVClientLoadException(String message, Exception exc) {
        super(message, exc);
    }

    public KVClientLoadException(String message) {
        super(message);
    }
}


