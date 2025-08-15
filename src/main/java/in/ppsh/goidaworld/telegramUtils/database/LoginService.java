package in.ppsh.goidaworld.telegramUtils.database;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;

public record LoginService(Dao<Login, Long> dao) {

    public Login getLogin(long id) {
        try { return dao.queryForId(id); }
        catch (SQLException e) { throw new RuntimeException(e); }
    }

    public Login createLogin(String ip, AuthUser user) {
        Login login = new Login(ip, user);
        try {
            dao.create(login);
            return login;
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public void updateLogin(Login login) {
        try { dao.update(login); }
        catch (SQLException e) { throw new RuntimeException(e);}
    }

    public boolean hasAcceptedLogin(String ip, AuthUser user) {
        try {
            return dao.queryBuilder()
                    .where().eq("ip", ip).and().eq("uuid", user.getUuid())
                    .and().eq("status", LogInStatus.ACCEPTED).countOf() > 0;
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public boolean hasBannedLogin(String ip, AuthUser user) {
        try {
            return dao.queryBuilder()
                    .where().eq("ip", ip).and().eq("uuid", user.getUuid())
                    .and().eq("status", LogInStatus.BANNED).countOf() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
