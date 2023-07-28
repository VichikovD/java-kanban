package util;

import java.io.IOException;

public class ManagerSaveException extends RuntimeException {
    public ManagerSaveException(String message, Exception exc) {
        super(message, exc);
    }
}
