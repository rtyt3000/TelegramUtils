package in.ppsh.goidaworld.telegramUtils.database;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.*;

import java.util.UUID;

@DatabaseTable(tableName = "auth_users")
@NoArgsConstructor
@RequiredArgsConstructor
public class AuthUser {
    @DatabaseField(id = true)
    @NonNull @Getter private UUID uuid;

    @DatabaseField(unique = true)
    @Getter @Setter private long telegramId;

    @SuppressWarnings("unused")
    @ForeignCollectionField
    private ForeignCollection<Login> logins;
}
