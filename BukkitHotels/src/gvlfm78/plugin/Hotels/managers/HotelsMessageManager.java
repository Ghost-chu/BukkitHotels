package gvlfm78.plugin.Hotels.managers;

import gvlfm78.plugin.Hotels.HotelsMain;
import gvlfm78.plugin.Hotels.handlers.HotelsConfigHandler;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

public class HotelsMessageManager {

	private HotelsMain plugin;
	public HotelsMessageManager(HotelsMain instance)
	{
		this.plugin = instance;
	}
	
	HotelsConfigHandler HConH = new HotelsConfigHandler(plugin);
	
	//Prefix
		YamlConfiguration locale = HConH.getLocale();
		String prefix = (locale.getString("chat.prefix").replaceAll("(?i)&([a-fk-r0-9])", "\u00A7$1")+" ");
		
	public String mes(String path){
		String mes = locale.getString(path);
		if(mes!=null){
		mes = (prefix+mes).replaceAll("(?i)&([a-fk-r0-9])", "\u00A7$1");
		}
		else
			mes = ChatColor.DARK_RED + "Message is null!";
		return mes;
	}
}
