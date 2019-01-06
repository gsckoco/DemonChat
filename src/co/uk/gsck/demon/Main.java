package co.uk.gsck.demon;

import net.milkbowl.vault.chat.Chat;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Logger;

public class Main extends JavaPlugin {

    Logger logger;
    public YamlConfiguration config;
    public HashMap<UUID, String> nicknames = new HashMap<>();
    public HashMap<UUID, Integer> warnings = new HashMap<>();
    public HashMap<UUID, ArrayList> lastMessage = new HashMap<>();
    private static Chat chat = null;

    private boolean vaultChat(){
        RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
        if (rsp==null) return false;
        chat = rsp.getProvider();
        return  chat != null;
    }

    @Override
    public void onEnable() {
        logger = Bukkit.getLogger();

        if (!this.getDataFolder().exists()) this.getDataFolder().mkdir();
        //UTF-8
        URL confUrl = getClass().getResource("/co/uk/gsck/demon/config.yml");
        File confDest = new File(this.getDataFolder().getAbsolutePath() + "/config.yml");
        if(!confDest.exists()) {
            try {
                FileUtils.copyURLToFile(confUrl, confDest);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        File confFile = new File(this.getDataFolder(), "config.yml");
        config = YamlConfiguration.loadConfiguration(confFile);
        if (vaultChat()) {
            Bukkit.getPluginManager().registerEvents(new onChat(this,config,chat), this);
        } else {
            logger.info("An error has occurred! Either vault or a permission plugin was not found. Prefixes and suffixes will not work!");
            Bukkit.getPluginManager().registerEvents(new onChat(this,config,null), this);
        }
        Bukkit.getPluginManager().registerEvents(new onJoinLeave(this,config,null), this);

        for(Player player : Bukkit.getServer().getOnlinePlayers()){
            File userdata = new File(getDataFolder(), File.separator + "PlayerDatabase");
            File f = new File(userdata, File.separator + player.getUniqueId().toString() + ".yml");
            FileConfiguration playerData = YamlConfiguration.loadConfiguration(f);
            if (!f.exists()) {
                try {
                    playerData.set("warnings", 0);
                    playerData.set("nickname", "none");
                    playerData.save(f);
                } catch (IOException exception) {

                    exception.printStackTrace();
                }
            }
            warnings.put(player.getUniqueId(),playerData.getInt("warnings"));
            nicknames.put(player.getUniqueId(),playerData.getString("nickname"));
        }
    }

    @Override
    public void onDisable() {

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equalsIgnoreCase("nickname")) {
            if (args.length==1) {
                if(!(sender instanceof Player)) {
                    sender.sendMessage("§cIncorrect usage, §c/nickname <player> <nickname>");
                    return true;
                }
                Player player = (Player) sender;
                if(!sender.hasPermission("demonchat.nickname.self")) {
                    player.sendMessage("§cError! You do not have the correct permission to use this.");
                    return true;
                }

                if(args[0].length() >= config.getInt("nicknameMaxLength")) {
                    sender.sendMessage("§cError! This nickname is too long.");
                    return true;
                }

                nicknames.put(player.getUniqueId(),args[0]);
                sender.sendMessage("§e"+sender.getName()+"§c's nickname is now set to "+args[0]);
                return true;
            } else if (args.length==2) {
                if(!sender.hasPermission("demonchat.nickname.others")) {
                    sender.sendMessage("§cError! You do not have the correct permission to use this.");
                    return true;
                }
                Player player = Bukkit.getServer().getPlayer(args[0]);
                if(player!=null){
                    if(args[1].length() >= config.getInt("nicknameMaxLength")) {
                        sender.sendMessage("§cError! This nickname is too long.");
                        return true;
                    }
                    nicknames.put(player.getUniqueId(),args[1]);
                    sender.sendMessage("§e"+player.getName()+"§c's nickname is now set to "+args[1]);
                    return true;
                }else{
                    sender.sendMessage("§eUnknown player "+args[0]+".");
                    return true;
                }
            } else {
                sender.sendMessage("§cIncorrect usage, §c/nickname [player] <nickname>");
                return true;
            }
        }else if(command.getName().equalsIgnoreCase("clearchat")) {
            if(!sender.hasPermission("demonchat.clearchat")) {
                sender.sendMessage("§cError! You do not have the correct permission to use this.");
                return true;
            }
            for (int i=1;i<100; i++) {
                Bukkit.broadcastMessage("§7-");
            }
            Bukkit.broadcastMessage("§c[DemonChat]  §aChat cleared by §c"+sender.getName());
        }
        return false;
    }
}
