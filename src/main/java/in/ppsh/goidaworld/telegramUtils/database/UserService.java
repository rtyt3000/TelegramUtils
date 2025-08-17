package in.ppsh.goidaworld.telegramUtils.database;

import com.j256.ormlite.dao.Dao;
import lombok.SneakyThrows;

import java.util.UUID;

public record UserService(Dao<AuthUser, UUID> dao) {

    @SneakyThrows
    public AuthUser getUser(UUID uuid) { return dao.queryForId(uuid); }

    @SneakyThrows
    public AuthUser createUser(UUID uuid) {
        AuthUser user = new AuthUser(uuid);
        dao.create(user);
        return user;
    }

    @SneakyThrows
    public void updateUser(AuthUser user) {
        dao.update(user);
    }
}
