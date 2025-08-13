package in.ppsh.goidaworld.telegramUtils.database;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.io.File;
import java.sql.SQLException;
import java.util.UUID;

public class DatabaseManager {
    private final ConnectionSource connection;
    private final Dao<AuthUser, UUID> authDao;
    private final Dao<Login, Long> loginDao;

    public DatabaseManager(File pluginFolder) throws SQLException {
        File dbFile = new File(pluginFolder, "auth.db");
        String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();

        this.connection = new JdbcConnectionSource(url);
        TableUtils.createTableIfNotExists(connection, AuthUser.class);
        TableUtils.createTableIfNotExists(connection, Login.class);

        this.authDao = DaoManager.createDao(connection, AuthUser.class);
        this.loginDao = DaoManager.createDao(connection, Login.class);
    }

    public Dao<AuthUser, UUID> getAuthDao() {
        return authDao;
    }
    public Dao<Login, Long> getLoginDao() {
        return loginDao;
    }

    public AuthUser getAuthUser(UUID uuid) {
        try {
            return authDao.queryForId(uuid);
        } catch (SQLException e) {
            return null;
        }
    }
    public void saveAuthUser(AuthUser user) {
        try {
            authDao.createOrUpdate(user);
        } catch (SQLException e) {
            return;
        }
    }

    public void close() {
        try {
            connection.close();
        } catch (Exception ignored) {}
    }
}