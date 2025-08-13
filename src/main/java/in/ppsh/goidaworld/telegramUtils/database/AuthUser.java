package in.ppsh.goidaworld.telegramUtils.database;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.UUID;

@DatabaseTable(tableName = "auth_users")
public class AuthUser {
    @DatabaseField(id = true)
    private UUID uuid;

    @DatabaseField(unique = true)
    private long telegramId;

    @ForeignCollectionField
    private ForeignCollection<Login> logins;

    public AuthUser() { }

    public AuthUser(UUID uuid) {
        this.uuid = uuid;
    }

    // Геттеры и сеттеры
    public UUID getUuid() {
        return uuid;
    }

    public long getTelegramId() {
        return telegramId;
    }

    public void setTelegramId(long telegramId) {
        this.telegramId = telegramId;
    }

    public ForeignCollection<Login> getSessions() {
        return logins;
    }
}
