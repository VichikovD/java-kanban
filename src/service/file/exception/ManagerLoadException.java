package service.file.exception;

public class ManagerLoadException extends RuntimeException {
    public ManagerLoadException(String message, Exception exc) {
        super(message, exc);
    }
}
