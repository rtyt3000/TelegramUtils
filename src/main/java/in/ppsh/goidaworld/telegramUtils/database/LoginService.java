package in.ppsh.goidaworld.telegramUtils.database;

import com.j256.ormlite.dao.Dao;
import lombok.SneakyThrows;

public record LoginService(Dao<Login, Long> dao) {

    @SneakyThrows
    public Login getLogin(long id) {
        return dao.queryForId(id);
    }

    @SneakyThrows
    public Login createLogin(String ip, AuthUser user) {
        Login login = new Login(ip, user);
        dao.create(login);
        return login;
    }

    @SneakyThrows
    public void updateLogin(Login login) {
        dao.update(login);
    }

    @SneakyThrows
    public boolean hasAcceptedLogin(String ip, AuthUser user) {
        return dao.queryBuilder()
                .where().eq("ip", ip).and().eq("uuid", user.getUuid())
                .and().eq("status", LogInStatus.ACCEPTED).countOf() > 0;
    }

    @SneakyThrows
    public boolean hasBannedLogin(String ip, AuthUser user) {
        return dao.queryBuilder()
                .where().eq("ip", ip).and().eq("uuid", user.getUuid())
                .and().eq("status", LogInStatus.BANNED).countOf() > 0;
    }
}
