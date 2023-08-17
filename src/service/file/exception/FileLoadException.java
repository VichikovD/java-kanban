package service.file.exception;

public class FileLoadException extends RuntimeException {

    public FileLoadException(String message, Exception exc) {
        super(message, exc);
    }
}
