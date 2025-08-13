package in.ppsh.goidaworld.telegramUtils.listeners;

import in.ppsh.goidaworld.telegramUtils.AuthManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Objects;
import java.util.logging.Logger;

public class JoinListener implements org.bukkit.event.Listener {
    private final Logger logger;
    private final AuthManager authManager;

    public JoinListener(AuthManager authManager, Logger logger) {
        this.logger = logger;
        this.authManager = authManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        try {
            String ip = Objects.requireNonNull(event.getPlayer().getAddress()).getAddress().getHostAddress();
            authManager.auth(event.getPlayer(), ip);
        } catch (Exception e) {
            logger.warning("Failed to get player ip address");
            logger.warning(e.getMessage());
        }
    }
}
