package service.file.exception;

public class ManagerSaveException extends RuntimeException {

    public ManagerSaveException(String message, Exception exc) {
        super(message, exc);
    }
}
