package in.ppsh.goidaworld.telegramUtils.database;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.UUID;

public record UserService(Dao<AuthUser, UUID> dao) {

    public AuthUser getUser(UUID uuid) {
        try { return dao.queryForId(uuid); }
        catch (SQLException e) { throw new RuntimeException(e); }
    }

    public AuthUser createUser(UUID uuid) {
        AuthUser user = new AuthUser(uuid);
        try {
            dao.create(user);
            return user;
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public void updateUser(AuthUser user) {
        try { dao.update(user); }
        catch (SQLException e) { throw new RuntimeException(e); }
    }
}
