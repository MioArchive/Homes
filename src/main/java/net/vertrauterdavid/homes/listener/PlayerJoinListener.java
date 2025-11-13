package net.vertrauterdavid.homes.listener;

import net.vertrauterdavid.homes.Homes;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        Bukkit.getScheduler().runTaskAsynchronously(Homes.getInstance(), () -> {
            if (!(Homes.getInstance().getSqlConnection().get("Homes", "UUID", "UUID='" + player.getUniqueId() + "'").equalsIgnoreCase(player.getUniqueId().toString()))) {
                Homes.getInstance().getSqlConnection().update("INSERT INTO Homes (UUID) VALUES ('" + player.getUniqueId() + "')");
            }
            Homes.getInstance().getHomeManager().loadLocal(player.getUniqueId());
        });
    }

}
