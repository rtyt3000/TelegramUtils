package in.ppsh.goidaworld.telegramUtils.listeners;

import in.ppsh.goidaworld.telegramUtils.AuthManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Objects;
import java.util.logging.Logger;

public record JoinListener(AuthManager authManager, Logger logger) implements Listener {

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
