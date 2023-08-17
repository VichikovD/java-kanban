package service.file.exception;

public class FileSaveException extends RuntimeException {

    public FileSaveException(String message, Exception exc) {
        super(message, exc);
    }
}
