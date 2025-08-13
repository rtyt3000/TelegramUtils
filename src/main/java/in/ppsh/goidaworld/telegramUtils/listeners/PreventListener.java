package in.ppsh.goidaworld.telegramUtils.listeners;

import in.ppsh.goidaworld.telegramUtils.AuthManager;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.logging.Logger;

public class PreventListener implements org.bukkit.event.Listener{
    private final Logger logger;
    private final AuthManager authManager;

    public PreventListener(AuthManager authManager, Logger logger) {
        this.logger = logger;
        this.authManager = authManager;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (authManager.isPlayerFrozen(event.getPlayer().getUniqueId())) {
            event.setTo(event.getFrom());
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        if (authManager.isPlayerFrozen(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (authManager.isPlayerFrozen(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof org.bukkit.entity.Player player) {
            if (authManager.isPlayerFrozen(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof org.bukkit.entity.Player player) {
            if (authManager.isPlayerFrozen(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }
}
