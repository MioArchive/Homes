package net.vertrauterdavid.homes;

import lombok.Getter;
import net.vertrauterdavid.homes.command.HomeCommand;
import net.vertrauterdavid.homes.database.SqlConnection;
import net.vertrauterdavid.homes.listener.InventoryClickListener;
import net.vertrauterdavid.homes.listener.PlayerJoinListener;
import net.vertrauterdavid.homes.util.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
@SuppressWarnings("deprecation")
public class Homes extends JavaPlugin {

    @Getter
    private static Homes instance;
    private SqlConnection sqlConnection;

    private HomeUtil homeUtil;

    private ItemUtil setItem;
    private ItemUtil unSetItem;
    private ItemUtil noPermissionItem;
    private ItemUtil confirmItem;
    private ItemUtil bedItem;
    private ItemUtil cancelItem;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        instance = this;

        sqlConnection = new SqlConnection();
        sqlConnection.setup();
        sqlConnection.createTables();

        homeUtil = new HomeUtil();

        setItem = getConfigItem("Gui.Items.Set");
        unSetItem = getConfigItem("Gui.Items.UnSet");
        noPermissionItem = getConfigItem("Gui.Items.NoPermission");
        confirmItem = getConfigItem("DeleteGui.Items.Confirm");
        bedItem = getConfigItem("DeleteGui.Items.Bed");
        cancelItem = getConfigItem("DeleteGui.Items.Cancel");

        Bukkit.getPluginManager().registerEvents(new InventoryClickListener(), instance);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(), instance);

        new HomeCommand("homes");
        new HomeCommand("home");
    }

    onDi

    public void openInventory(Player player) {
        int maxHomes = getConfig().getInt("Settings.MaxHomes", 5);
        Inventory inventory = Bukkit.createInventory(null, getConfig().getInt("Gui.Rows", 3) * 9, ConfigUtil.translateColorLEG(getConfig().getString("Gui.Title", "Homes")));

        for (int i = 1; i <= maxHomes; i++) {
            int slot = i + (maxHomes == 5 ? 1 : 0) + (inventory.getSize() == 27 ? 9 : 18);
            if (getAmount(player) >= i) {
                inventory.setItem(slot, (homeUtil.get(player.getUniqueId(), i) != null ? getSetItem(i) : getUnSetItem(i)));
            } else {
                inventory.setItem(slot, getNoPermissionItem(i));
            }
        }

        player.openInventory(inventory);
        ConfigUtil.playSound(player, "GuiSounds.OpenSound");
    }

    public void openDeleteInventory(Player player, int home) {
        Inventory inventory = Bukkit.createInventory(null, getConfig().getInt("DeleteGui.Rows", 3) * 9, ConfigUtil.translateColorLEG(getConfig().getString("DeleteGui.Title", "Homes")) + " " + home);

        inventory.setItem(getConfig().getInt("DeleteGui.Items.Confirm.Slot", 11), confirmItem.toItemStack());
        if (getConfig().getBoolean("DeleteGui.Items.Bed.Enabled", true)) {
            inventory.setItem(getConfig().getInt("DeleteGui.Items.Bed.Slot", 13), getBedItem(home));
        }
        inventory.setItem(getConfig().getInt("DeleteGui.Items.Cancel.Slot", 15), cancelItem.toItemStack());

        player.openInventory(inventory);
        ConfigUtil.playSound(player, "GuiSounds.OpenSound");
    }

    private ItemUtil getConfigItem(String path) {
        Material material = Material.valueOf(ConfigUtil.translateColorLEG(getConfig().getString(path + ".Material")));
        String name = ConfigUtil.translateColorLEG(getConfig().getString(path + ".Name"));
        List<String> lore = getConfig().getStringList(path + ".Lore");
        return new ItemUtil(material).setName(name).setLore(lore.stream().map(ConfigUtil::translateColorLEG).toArray(String[]::new));
    }

    private ItemStack getSetItem(int home) {
        return new ItemUtil(setItem).setName(setItem.getItemMeta().getDisplayName().replaceAll("%home%", String.valueOf(home))).toItemStack();
    }

    private ItemStack getUnSetItem(int home) {
        return new ItemUtil(unSetItem).setName(unSetItem.getItemMeta().getDisplayName().replaceAll("%home%", String.valueOf(home))).toItemStack();
    }

    private ItemStack getNoPermissionItem(int home) {
        return new ItemUtil(noPermissionItem).setName(noPermissionItem.getItemMeta().getDisplayName().replaceAll("%home%", String.valueOf(home))).toItemStack();
    }

    private ItemStack getBedItem(int home) {
        return new ItemUtil(bedItem).setName(bedItem.getItemMeta().getDisplayName().replaceAll("%home%", String.valueOf(home))).toItemStack();
    }

    public int getAmount(final @NotNull Player player) {
        for (int i = 7; i >= 1; i--) {
            if (player.hasPermission("homes." + i)) {
                return i;
            }
        }
        return 1;
    }

}
