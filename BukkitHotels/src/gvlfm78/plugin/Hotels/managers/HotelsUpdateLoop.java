package gvlfm78.plugin.Hotels.managers;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import gvlfm78.plugin.Hotels.HotelsMain;
import gvlfm78.plugin.Hotels.HotelsUpdateChecker;
import gvlfm78.plugin.Hotels.handlers.HotelsConfigHandler;

public class HotelsUpdateLoop extends BukkitRunnable{
	HotelsMain plugin;
	
	public HotelsUpdateLoop(HotelsMain instance) {
		this.plugin = instance;
	}
	HotelsMessageManager HMM = new HotelsMessageManager(plugin);
	HotelsConfigHandler HConH = new HotelsConfigHandler(plugin);
	
	@Override
	public void run() {
	
		HotelsUpdateChecker updateChecker = new HotelsUpdateChecker(plugin, "http://dev.bukkit.org/bukkit-plugins/hotels/files.rss");
		updateChecker.updateNeeded();
		if(HConH.getconfigyml().getBoolean("settings.checkForUpdates")){
			if(updateChecker.updateNeeded()){
				String updateAvailable = HMM.mesnopre("main.updateAvailable").replaceAll("%version%", updateChecker.getVersion());
				String updateLink = HMM.mesnopre("main.updateAvailableLink").replaceAll("%link%", updateChecker.getLink());
				plugin.getLogger().info(updateAvailable);
				plugin.getLogger().info(updateLink);
				
				YamlConfiguration queue = HConH.getMessageQueue();

				queue.set("messages.update.available", updateAvailable);
				queue.set("messages.update.link", updateLink);
				HConH.saveMessageQueue(queue);
			}
		}
		
	}

}
