package framework.utilitaire;

import java.sql.Connection;

public abstract class Model {
    
    public void insert(Connection conn) throws Exception {
        DBUtil.insert(this, conn);
    }

    public static <T> java.util.List<T> getAll(Class<T> clazz, Connection conn) throws Exception {
        return DBUtil.getAll(clazz, conn);
    }
}
