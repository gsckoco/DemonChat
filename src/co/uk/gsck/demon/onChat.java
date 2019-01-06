package co.uk.gsck.demon;

import net.milkbowl.vault.chat.Chat;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class onChat implements Listener {

    private Main plugin;
    private YamlConfiguration config;
    private Chat chat;

    public onChat(Main plugin, YamlConfiguration config, Chat chat) {
        this.plugin = plugin;
        this.config = config;
        this.chat = chat;
    }

    private String filterBadWords(String message, Player player){
        String filtered = message;
        List<String> betterWords = config.getStringList("betterWords");
        int wordSize = betterWords.size();
        boolean warn = false;
        for(String entry : config.getStringList("badWords")){
            Random r = new Random();
            filtered = filtered.replaceAll("(?i)"+entry, betterWords.get(r.nextInt(wordSize)));

            if (message.toLowerCase().contains(entry.toLowerCase())) {
                warn = true;
            }
        }
        if (warn) {
            if (plugin.warnings.get(player.getUniqueId()) == null ) {
                plugin.warnings.put(player.getUniqueId(),1);
            } else {
                int warnings = plugin.warnings.get(player.getUniqueId());
                warnings++;

                if (warnings>=config.getInt("warningsForBan")) {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        if(player.hasPermission("demonchat.bypassbans")) {
                            player.kickPlayer("§cYou have been warned not to swear!");
                        }else{
                            player.kickPlayer("§cYou have been warned not to swear!\n§eYou have been §c§lBANNED.");
                            Bukkit.getBanList(BanList.Type.NAME).addBan(player.getName(), "§cYou have been warned not to swear!\n§eYou have been §c§lBANNED.", null, "DemonChat");
                        }
                    });
                }else if (warnings==config.getInt("warningsForKick")) {
                    Bukkit.getScheduler().runTask(plugin, () -> player.kickPlayer("§cYou have been warned not to swear!\n§eIf you keep this up you will be §c§lBANNED."));
                }

                plugin.warnings.put(player.getUniqueId(),warnings);
            }
            player.sendMessage("§cPlease do not swear! Warning " + plugin.warnings.get(player.getUniqueId()) + "/" + config.getInt("warningsForBan"));
        }
        return filtered;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        e.setCancelled(true);
        Player player = e.getPlayer();
        String message = e.getMessage();

        ArrayList lastMessage = plugin.lastMessage.get(player.getUniqueId());

        if (lastMessage!=null) {
            if ((System.currentTimeMillis()-(long)lastMessage.get(0)) <= 1000*config.getLong("timeBetweenMessages") ) {
                player.sendMessage("§cSlow down. You are sending messages too fast. You have been warned");
                return;
            }
        }

        String format = config.getString("chatFormat").replace("&","§");
        String nickname = plugin.nicknames.get(player.getUniqueId());
        String username = player.getName();

        if (nickname.trim().equalsIgnoreCase("none")) {
            username=config.getString("nicknamePrefix") + nickname;
        }
        String prefix = "§c§;ERR§f";
        if (chat!=null) prefix = chat.getPlayerPrefix(player).replace("&","§");
        message = filterBadWords(message,player);

        if (player.isOp()) {
            String nameCol = config.getString("opNameColour");
            String msgCol = config.getString("opChatColour");
            if (!nameCol.equalsIgnoreCase("")) username = "§" + nameCol + username;
            if (!msgCol.equalsIgnoreCase("")) message = "§" + msgCol + message;
        }

        if (player.hasPermission("demonchat.chat.colour")) {
            message = message.replace("&","§");
        }

        format = format.replace("{WORLD}",player.getWorld().getName());
        format = format.replace("{USERNAME}",username);
        format = format.replace("{MESSAGE}",message);
        format = format.replace("{PREFIX}", prefix);

        Bukkit.broadcastMessage(format);
        ArrayList info = new ArrayList();

        info.add(0,System.currentTimeMillis());// Tick
        info.add(1,message);

        plugin.lastMessage.put(player.getUniqueId(),info);
    }
}
