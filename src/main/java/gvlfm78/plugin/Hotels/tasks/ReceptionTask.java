package gvlfm78.plugin.Hotels.tasks;

import java.io.File;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import gvlfm78.plugin.Hotels.HotelsMain;
import gvlfm78.plugin.Hotels.handlers.HotelsConfigHandler;
import gvlfm78.plugin.Hotels.managers.HotelsFileFinder;
import gvlfm78.plugin.Hotels.managers.Mes;
import gvlfm78.plugin.Hotels.managers.SignManager;

public class ReceptionTask extends BukkitRunnable{

	private HotelsMain plugin;
	private SignManager SM;
	
	public ReceptionTask(HotelsMain plugin){
		this.plugin = plugin;
		SM = new SignManager(plugin);
	}

	//Task to update reception signs
	@Override
	public void run(){		
		ArrayList<String> fileList = HotelsFileFinder.listFiles("plugins"+File.separator+"Hotels"+File.separator+"Signs"+File.separator+"Reception");
		if(fileList==null) return;
		for(String x : fileList){//Looping through all files in Signs directory
			File file = HotelsConfigHandler.getReceptionFile(x);
			Mes.debugConsole("Reception sign getting checked: " + file.getName() + " Path: " + file.getAbsolutePath());
			if(file.getName().matches("Reception-.+-.+")){
				//It's a reception sign
				YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
				World world = Bukkit.getWorld(config.getString("Reception.location.world").toLowerCase());
				int locx = config.getInt("Reception.location.x");
				int locy = config.getInt("Reception.location.y");
				int locz = config.getInt("Reception.location.z");
				Block b = world.getBlockAt(locx,locy,locz);
				Location l = b.getLocation();
				if(SM.updateReceptionSign(l) == true){
					file.delete();
					b.setType(Material.AIR);
					plugin.getLogger().info(Mes.mesnopre("sign.delete.reception").replaceAll("%filename%", file.getName()));
				}
			}
		}
	}
}
