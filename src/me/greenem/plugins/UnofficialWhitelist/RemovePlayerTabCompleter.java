package me.greenem.plugins.UnofficialWhitelist;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.YamlConfiguration;

public class RemovePlayerTabCompleter implements TabCompleter {
	public File dataDirectory;
	public File whitelistDataDirectory;
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		dataDirectory = new File(Bukkit.getServer().getWorldContainer(), "files/data");
		whitelistDataDirectory = new File(dataDirectory, "whitelistData");
		
		List<String> list = new ArrayList<String>();
		if (args.length == 1) {
			String[] players = getWhitelistedPlayers();
			for (String i : players) {
				list.add(i);
			}
			removeOther(list, args, 0);
		}
		return list;
		//}
		//return null;
	}
	
	private void removeOther(List<String> list, String[] args, int pos) {
		for (Iterator<String> it = list.iterator(); it.hasNext();) {
			String s = it.next();
			if (args[pos] != null && !s.startsWith(args[pos])) {
				it.remove();
			}
		}
	}
	
	//COPYED VERSION
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
}