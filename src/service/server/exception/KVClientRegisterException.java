package service.server.exception;

//  Объяснения причины создания исключения при регистрации в классе KVTaskClient
public class KVClientRegisterException extends RuntimeException {

    public KVClientRegisterException(String message, Exception exc) {
        super(message, exc);
    }

    public KVClientRegisterException(String message) {
        super(message);
    }
}
