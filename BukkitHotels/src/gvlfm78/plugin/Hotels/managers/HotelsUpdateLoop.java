package gvlfm78.plugin.Hotels.managers;

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
				String updateAvailable = locale.getString("main.updateAvailable").replaceAll("%version%", this.updateChecker.getVersion());
				String updateLink = locale.getString("main.updateAvailableLink").replaceAll("%link%", this.updateChecker.getLink());
				getLogger().info(updateAvailable);
				getLogger().info(updateLink);

				queue.set("messages.update.available", updateAvailable);
				queue.set("messages.update.link", updateLink);
				HConH.saveMessageQueue(queue);
			}
		}
		
	}

}
