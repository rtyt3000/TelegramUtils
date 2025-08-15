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
    @SuppressWarnings("FieldCanBeLocal")
    private final Dao<AuthUser, UUID> authDao;
    private final Dao<Login, Long> loginDao;
    public final UserService userService;
    public final LoginService loginService;

    public DatabaseManager(File pluginFolder) throws SQLException {
        File dbFile = new File(pluginFolder, "auth.db");
        String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();

        this.connection = new JdbcConnectionSource(url);
        TableUtils.createTableIfNotExists(connection, AuthUser.class);
        TableUtils.createTableIfNotExists(connection, Login.class);

        this.authDao = DaoManager.createDao(connection, AuthUser.class);
        this.loginDao = DaoManager.createDao(connection, Login.class);
        this.userService = new UserService(authDao);
        this.loginService = new LoginService(loginDao);

    }

    public Dao<Login, Long> getLoginDao() {
        return loginDao;
    }

    public void close() {
        try {
            connection.close();
        } catch (Exception ignored) {}
    }
}