package util;

import java.io.IOException;

public class ManagerLoadException extends RuntimeException {
    public ManagerLoadException(String message, Exception exc) {
        super(message, exc);
    }
}
