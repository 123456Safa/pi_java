package Services;

import java.sql.SQLDataException;
import java.util.List;

public interface Iservice <T> {
    void ajouter (T t) throws SQLDataException;
    void supprimer (T t) throws SQLDataException;
    void modifier (T t) throws SQLDataException;
    List<T> recuperer () throws SQLDataException;
}
