package service.file.exception;

public class ManagerSaveException extends RuntimeException {
    String fileName;

    public ManagerSaveException(String message, Exception exc, String fileName) {
        super(message, exc);
        this.fileName = fileName;
    }
}
