package in.ppsh.goidaworld.telegramUtils.utils;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

public class FreezeManager {
    private final Set<UUID> frozenPlayers = new HashSet<>();
    private final Logger logger;

    public FreezeManager(Logger logger) {
        this.logger = logger;
    }

    public void freezePlayer(UUID uuid) {
        frozenPlayers.add(uuid);
        logger.info("Player " + uuid + " has been frozen.");
    }

    public void unfreezePlayer(UUID uuid) {
        frozenPlayers.remove(uuid);
        logger.info("Player " + uuid + " has been unfrozen.");
    }

    public boolean isPlayerFrozen(UUID uuid) {
        return frozenPlayers.contains(uuid);
    }
}
