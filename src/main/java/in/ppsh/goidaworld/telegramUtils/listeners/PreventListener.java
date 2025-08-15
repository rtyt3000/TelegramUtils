package in.ppsh.goidaworld.telegramUtils.listeners;

import in.ppsh.goidaworld.telegramUtils.utils.FreezeManager;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.logging.Logger;

public record PreventListener(FreezeManager freezeManager, Logger logger) implements Listener {

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (freezeManager.isPlayerFrozen(event.getPlayer().getUniqueId())) {
            event.setTo(event.getFrom());
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        if (freezeManager.isPlayerFrozen(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (freezeManager.isPlayerFrozen(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (freezeManager.isPlayerFrozen(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (freezeManager.isPlayerFrozen(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }
}
