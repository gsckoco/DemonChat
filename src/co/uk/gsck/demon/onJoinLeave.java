package co.uk.gsck.demon;

import net.milkbowl.vault.chat.Chat;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.*;

public class onJoinLeave implements Listener {

    private Main plugin;
    private YamlConfiguration config;
    private Chat chat;

    public onJoinLeave(Main plugin, YamlConfiguration config, Chat chat) {
        this.plugin = plugin;
        this.config = config;
        this.chat = chat;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {

        Player p = e.getPlayer();

        File userdata = new File(plugin.getDataFolder(), File.separator + "PlayerDatabase");
        File f = new File(userdata, File.separator + p.getUniqueId().toString() + ".yml");
        FileConfiguration playerData = YamlConfiguration.loadConfiguration(f);

        if (!f.exists()) {
            try {
                playerData.set("warnings", 0);
                playerData.set("nickname", "");
                playerData.save(f);
            } catch (IOException exception) {

                exception.printStackTrace();
            }
        }

        plugin.warnings.put(p.getUniqueId(),playerData.getInt("warnings"));
        plugin.nicknames.put(p.getUniqueId(),playerData.getString("nickname"));
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();

        File userdata = new File(plugin.getDataFolder(), File.separator + "PlayerDatabase");
        File f = new File(userdata, File.separator + p.getUniqueId().toString() + ".yml");
        FileConfiguration playerData = YamlConfiguration.loadConfiguration(f);

        playerData.set("warnings", plugin.warnings.get(e.getPlayer().getUniqueId()));
        playerData.set("nickname", plugin.nicknames.get(e.getPlayer().getUniqueId()));
        try {
            playerData.save(f);
        } catch (IOException e1) {}
        plugin.warnings.remove(e.getPlayer().getUniqueId());
        plugin.nicknames.remove(e.getPlayer().getUniqueId());
        plugin.lastMessage.remove(e.getPlayer().getUniqueId());
    }
}