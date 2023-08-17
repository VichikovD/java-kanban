package service.server.exception;

import java.io.IOException;

public class ValidateException extends Exception {
    public ValidateException(String message, Throwable e) {
        super(message,e);
    }

   public ValidateException(String message) {
       super(message);
   }
}
