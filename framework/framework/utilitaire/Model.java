package framework.utilitaire;

import java.sql.Connection;

public abstract class Model {

    public void insert(Connection conn) throws Exception {
        DBUtil.insert(this, conn);
    }

    public void update(Connection conn) throws Exception {
        DBUtil.update(this, conn);
    }

    public static <T> java.util.List<T> getAll(Class<T> clazz, Connection conn) throws Exception {
        return DBUtil.getAll(clazz, conn);
    }

    public static <T> T getById(Class<T> clazz, Object id, Connection conn) throws Exception {
        return DBUtil.getById(clazz, id, conn);
    }

    public void delete(Connection conn) throws Exception {
        DBUtil.delete(this, conn);
    }

    public static void deleteById(Class<?> clazz, Object id, Connection conn) throws Exception {
        DBUtil.deleteById(clazz, id, conn);
    }
}
