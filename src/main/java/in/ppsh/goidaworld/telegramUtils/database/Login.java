package in.ppsh.goidaworld.telegramUtils.database;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@DatabaseTable(tableName = "logins")
@NoArgsConstructor
public class Login {
    @Getter
    @DatabaseField(generatedId = true)
    long id;

    @Getter
    @DatabaseField
    private String ip;

    @DatabaseField
    private long time;

    @Getter
    @DatabaseField(foreign = true, columnName = "uuid", canBeNull = false)
    private AuthUser user;

    @Setter
    @DatabaseField
    private LogInStatus status;

    public Login(String ip, AuthUser user) {
        this.ip = ip;
        this.time = System.currentTimeMillis();
        this.status = LogInStatus.PENDING;
        this.user = user;
    }

}