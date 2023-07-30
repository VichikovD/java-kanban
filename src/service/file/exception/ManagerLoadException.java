package service.file.exception;

public class ManagerLoadException extends RuntimeException {
    String fileName;

    public ManagerLoadException(String message, Exception exc, String fileName) {
        super(message, exc);
        this.fileName = fileName;
    }
}
