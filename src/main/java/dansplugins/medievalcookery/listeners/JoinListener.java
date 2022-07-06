package dansplugins.medievalcookery.listeners;

import dansplugins.medievalcookery.MedievalCookery;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {
    private final MedievalCookery medievalCookery;

    public JoinListener(MedievalCookery medievalCookery) {
        this.medievalCookery = medievalCookery;
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        medievalCookery.endPlayerEating(event.getPlayer());
    }
}
