package me.greenem.plugins.UnofficialWhitelist;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.bukkit.event.Listener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.plugin.java.JavaPlugin;

public class MainFile extends JavaPlugin implements Listener {
	Logger log = getLogger(); 
	
	public File dataDirectory;
	public File whitelistDataDirectory;
	
	@Override
	public void onDisable() {
		log(ChatColor.RED + "UnofficialWhitelist plugin has been disabled.");
	}	

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
		//log(Bukkit.getVersion());
		/*String color = "";
		if(!Bukkit.getVersion().contains("Bukkit")) {
			color += ChatColor.GREEN;
		}
		log(color + "UnofficialWhitelist plugin has been enabled!");*/
		log(ChatColor.GREEN + "UnofficialWhitelist plugin has been enabled!");
		
		dataDirectory = new File(getServer().getWorldContainer(), "files/data");
		whitelistDataDirectory = new File(dataDirectory, "whitelistData");
		
		whitelistDataDirectory.mkdirs();
		
		getCommand("uw-remove").setTabCompleter(new RemovePlayerTabCompleter());
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		if(cmd.getName().equalsIgnoreCase("uw-enable") || cmd.getName().equalsIgnoreCase("uw-disable") || cmd.getName().equalsIgnoreCase("uw-add")
		|| cmd.getName().equalsIgnoreCase("uw-remove") || cmd.getName().equalsIgnoreCase("uw-state") || cmd.getName().equalsIgnoreCase("uw-allowed-list")) {
			if(!(sender instanceof ConsoleCommandSender)) {
				sender.sendMessage(ChatColor.RED + "This comand is only for console.");
				return true;
			}
		}
		if(cmd.getName().equalsIgnoreCase("uw-enable")){
			setWhitelistEnabled(true);
		}
		else if(cmd.getName().equalsIgnoreCase("uw-disable")){
			setWhitelistEnabled(false);
		}
		else if(cmd.getName().equalsIgnoreCase("uw-add")){
			addToWhitelist(sender, args);
			if(!getWhitelistEnabled()) {
				sender.sendMessage("[Note] The whitelist is currently disabled");
			}
		}
		else if(cmd.getName().equalsIgnoreCase("uw-remove")){
			removeFromWhitelist(sender, args);
			if(!getWhitelistEnabled()) {
				sender.sendMessage("[Note] The whitelist is currently disabled");
			}
		}
		else if(cmd.getName().equalsIgnoreCase("uw-state")){
			String s = ChatColor.WHITE + "The whitelist is";
			if(getWhitelistEnabled()) {
				s += ChatColor.GREEN + "ENABLED";
			}
			else {
				s += ChatColor.RED + "DISABLED";
			}
			log(s);
		}
		else if(cmd.getName().equalsIgnoreCase("uw-allowed-list")){
			String s = ChatColor.WHITE + "The list of whitelisted players:\n";
			String[] players = getWhitelistedPlayers();
			if(players!=null && players.length>0) {
				for (int i = 0; i < players.length; i++) {
					s += ChatColor.AQUA + players[i];
					if(i+1<players.length) {
						 s += ChatColor.WHITE + ", ";
					}
				}
			}
			else {
				s = ChatColor.WHITE + "There are no whitelisted players.\n";
			}
			sender.sendMessage(s);
		}
		return false;
	}

	public static boolean isInteger(String s) {
	    try { 
	        Integer.parseInt(s); 
	    } catch(NumberFormatException e) { 
	        return false; 
	    } catch(NullPointerException e) {
	        return false;
	    }
	    // only got here if we didn't return false
	    return true;
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void BlockOrAllow(PlayerLoginEvent e) {
		//log("PlayerLoginEvent");
			if(getWhitelistEnabled()) {
			Player p = e.getPlayer();
			boolean good = isPlayerWhitelisted(p.getName()); 
			if(!good) {
				e.disallow(Result.KICK_OTHER, "You are not whitelisted on the server!");
			}
		}	
	}
	
	public boolean getWhitelistEnabled() {
		YamlConfiguration c = null;
		boolean enabled = false;
		c = YamlConfiguration.loadConfiguration(new File(whitelistDataDirectory, "otherData.yml"));
		try{
			enabled = c.getBoolean("thispluginenabled");
		}
		catch(NullPointerException e1) {
			//ignore
		}
		return enabled;
	}
	
	public void setWhitelistEnabled(boolean enabled) {
		if(getWhitelistEnabled()==enabled) {
			String s = ChatColor.YELLOW + "The Whitelist was already ";
			if(enabled) {
				s += "enabled!";
			}
			else {
				s += "disabled!";
			}
			log(s);
			return;
		}
		YamlConfiguration c = null;
		c = YamlConfiguration.loadConfiguration(new File(whitelistDataDirectory, "otherData.yml"));
		c.set("thispluginenabled", enabled);
		try {
			c.save(new File(whitelistDataDirectory, "otherData.yml"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(enabled) {
			log(ChatColor.GREEN + "ENABLED the unofficial whitelist");
		}
		else {
			log(ChatColor.GREEN + "DISABLED the unofficial whitelist");
		}
	}
	
	public String[] getWhitelistedPlayers() {
		YamlConfiguration c = null;
		String[] content = null;
		c = YamlConfiguration.loadConfiguration(new File(whitelistDataDirectory, "allowedList.yml"));
		try{
			content = ((List<String>) c.get("whitelisted")).toArray(new String[0]);
		}
		catch(NullPointerException e1) {
			//ignore
		}
		return content;
	}
	
	public boolean isPlayerWhitelisted(String p) {
		String[] players = null;
		players = getWhitelistedPlayers();
		if(players!=null) {
			for (int i = 0; i<players.length; i++) {
				if(players[i].equals(p)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public void addToWhitelist(CommandSender sender, String[] args) {
		if(!(sender instanceof ConsoleCommandSender)) {
			sender.sendMessage(ChatColor.RED + "This comand is only for console.");
			return;
		}
		if(args.length!=1) {
			log(ChatColor.RED + "Arguments' length should be 1!");
			return;
		}
		String name = args[0];
		if(name!=null && isPlayerWhitelisted(name) == true) {
			log(ChatColor.YELLOW + "[Warning] The player is already whitelisted!");
			return;
		}
		YamlConfiguration c = new YamlConfiguration();
		String[] players = getWhitelistedPlayers();
		ArrayList<String> list = new ArrayList<>();
		if(players!=null) {
			for (int i = 0; i<players.length; i++) {
				list.add(players[i]);
			}
		}
		if(name!=null) {
			list.add(name);
		}
		c.set("whitelisted", list);
		try {
			c.save(new File(whitelistDataDirectory, "allowedList.yml"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		log(ChatColor.GREEN + "Successfully added " + args[0] + " to whitelist");
	}
	
	public void removeFromWhitelist(CommandSender sender, String[] args) {
		if(!(sender instanceof ConsoleCommandSender)) {
			sender.sendMessage(ChatColor.RED + "This comand is only for console.");
			return;
		}
		if(args.length!=1) {
			log(ChatColor.RED + "Arguments' length should be 1!");
			return;
		}
		String name = args[0];
		if(name!=null && isPlayerWhitelisted(name) == false) {
			log(ChatColor.YELLOW + "[Warning] The player is already NOT whitelisted!");
			return;
		}
		YamlConfiguration c = new YamlConfiguration();
		String[] players = getWhitelistedPlayers();
		ArrayList<String> list = new ArrayList<String>();
		if(players!=null) {
			for (int i = 0; i<players.length; i++) {
				list.add(players[i]);
			}
		}
		if(name!=null && list.contains(name)) {
			list.remove(name);
		}
		c.set("whitelisted", list);
		try {
			c.save(new File(whitelistDataDirectory, "allowedList.yml"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		log(ChatColor.GREEN + "Successfully removed " + args[0] + " from whitelist");
	}
	
	public void log(String s) {
		Bukkit.getConsoleSender().sendMessage(s);
	}
	
	public void log(int s) {
		log(s + "");
	}
	
	public void log(boolean s) {
		log(s + "");
	}
	
	public void log(double s) {
		log(s + "");
	}
	
	public void log(float s) {
		log(s + "");
	}
	
	public void log(char s) {
		log(s + "");
	}
	
	public void log(short s) {
		log(s + "");
	}
}