package in.ppsh.goidaworld.telegramUtils.database;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "logins")
public class Login {
    @DatabaseField(generatedId = true)
    long id;

    @DatabaseField
    private String ip;

    @DatabaseField
    private long time;

    @DatabaseField(foreign = true, columnName = "uuid", canBeNull = false)
    private AuthUser user;

    @DatabaseField
    @SuppressWarnings("unused")
    private LogInStatus status;

    public Login() {}

    public Login(String ip, AuthUser user) {
        this.ip = ip;
        this.time = System.currentTimeMillis();
        this.status = LogInStatus.PENDING;
        this.user = user;
    }

    public long getId() {
        return id;
    }

    public AuthUser getUser() {
        return user;
    }

    public void setStatus(LogInStatus status) {
        this.status = status;
    }

    public String getIp() {
        return ip;
    }
}