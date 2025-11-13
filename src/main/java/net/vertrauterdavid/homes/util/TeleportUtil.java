package net.vertrauterdavid.homes.util;

import lombok.experimental.UtilityClass;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.vertrauterdavid.homes.Homes;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

@UtilityClass
public class TeleportUtil {

    private final HashMap<UUID, Location> move = new HashMap<>();

    public void teleport(Player player, Location location) {
        player.closeInventory();

        if (Homes.getInstance().getConfig().getInt("Teleport.CoolDown") < 1 || player.hasPermission("homes.bypass")) {
            player.teleport(location);
            ConfigUtil.playSound(player, "Teleport.TeleportSound");
            return;
        }

        move.put(player.getUniqueId(), player.getLocation());

        final int[] seconds = {6};
        final UUID uuid = player.getUniqueId();
        new BukkitRunnable() {
            @Override
            public void run() {
                seconds[0]--;

                if (Bukkit.getPlayer(uuid) == null) {
                    move.remove(player.getUniqueId());
                    this.cancel();
                    return;
                }

                if (Homes.getInstance().getConfig().getBoolean("Teleport.CancelOnMove")) {
                    Location moveLocation = move.get(player.getUniqueId());
                    if (moveLocation != null && moveLocation.distance(player.getLocation()) > Homes.getInstance().getConfig().getDouble("Teleport.MaximumMoveDistance")) {

                        player.sendMessage(ConfigUtil.getPrefix().append(ConfigUtil.getMessage("Teleport.CancelMessage")));
                        if (!(Objects.requireNonNull(Homes.getInstance().getConfig().getString("Teleport.CancelTitle.Title")).equalsIgnoreCase("")) || !(Homes.getInstance().getConfig().getString("Teleport.CancelTitle.SubTitle").equalsIgnoreCase(""))) {
                            player.sendActionBar(ConfigUtil.getPrefix().append(ConfigUtil.getMessage("Teleport.CancelTitle.SubTitle")).append(ConfigUtil.getMessage("Teleport.CancelTitle.Title")));
                        }
                        ConfigUtil.playSound(player, "Teleport.CancelSound");
                        move.remove(player.getUniqueId());
                        this.cancel();
                        return;
                    }
                }

                switch (seconds[0]) {
                    case 5, 4, 3, 2, 1 -> {
                        if (!(Objects.requireNonNull(Homes.getInstance().getConfig().getString("Teleport.Message")).equalsIgnoreCase(""))) {
                            String message = Homes.getInstance().getConfig().getString("Teleport.Message", "").replaceAll("%seconds%", String.valueOf(seconds[0]));
                            player.sendMessage(ConfigUtil.getPrefix().append(ConfigUtil.translateColorCodes(message)));

                        }
                        if (!(Objects.requireNonNull(Homes.getInstance().getConfig().getString("Teleport.Actionbar")).equalsIgnoreCase(""))) {
                            String message = Homes.getInstance().getConfig().getString("Teleport.Actionbar", "").replaceAll("%seconds%", String.valueOf(seconds[0]));
                            player.sendMessage(ConfigUtil.translateColorCodes(message));
                        }
                        if (!(Objects.requireNonNull(Homes.getInstance().getConfig().getString("Teleport.Title.Title")).equalsIgnoreCase("")) || !(Objects.requireNonNull(Homes.getInstance().getConfig().getString("Teleport.Title.SubTitle")).equalsIgnoreCase(""))) {
                            String message = Homes.getInstance().getConfig().getString("Teleport.Title.Title", "").replaceAll("%seconds%", String.valueOf(seconds[0]));
                            player.sendActionBar(ConfigUtil.translateColorCodes(message));
                        }
                        ConfigUtil.playSound(player, "Teleport.CoolDownSound");
                    }
                    case 0 -> {
                        player.teleport(location);
                        ConfigUtil.playSound(player, "Teleport.CoolDownSound");
                        move.remove(player.getUniqueId());
                        this.cancel();
                    }
                }
            }
        }.runTaskTimer(Homes.getInstance(), 0, 20);
    }

}
