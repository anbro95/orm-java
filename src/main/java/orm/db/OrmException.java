package orm.db;

public class OrmException extends RuntimeException {

    public OrmException(String message) {
        super(message);
    }

    public OrmException(String message, Exception e) {
        super(message, e);
    }
}
