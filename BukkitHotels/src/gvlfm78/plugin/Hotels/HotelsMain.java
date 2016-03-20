package gvlfm78.plugin.Hotels;

import java.io.IOException;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import gvlfm78.plugin.Hotels.handlers.HotelsCommandHandler;
import gvlfm78.plugin.Hotels.handlers.HotelsConfigHandler;
import gvlfm78.plugin.Hotels.managers.HotelsLoop;
import net.milkbowl.vault.economy.Economy;

public class HotelsMain extends JavaPlugin{

	public static Economy economy = null; //Creating economy variable

	HotelsConfigHandler HConH = new HotelsConfigHandler(this);
	HotelsLoop hotelsloop;
	protected HotelsUpdateChecker updateChecker;

	YamlConfiguration locale = HConH.getLocale();
	YamlConfiguration queue = HConH.getMessageQueue();

	@Override
	public void onEnable(){
		setupConfig();
		this.updateChecker = new HotelsUpdateChecker(this, "http://dev.bukkit.org/bukkit-plugins/hotels/files.rss");
		this.updateChecker.updateNeeded();
		if(getConfig().getBoolean("settings.checkForUpdates")){
			if(this.updateChecker.updateNeeded()){
				String updateAvailable = locale.getString("main.updateAvailable").replaceAll("%version%", this.updateChecker.getVersion());
				String updateLink = locale.getString("main.updateAvailableLink").replaceAll("%link%", this.updateChecker.getLink());
				getLogger().info(updateAvailable);
				getLogger().info(updateLink);

				queue.set("messages.update.available", updateAvailable);
				queue.set("messages.update.link", updateLink);
				HConH.saveMessageQueue(queue);
			}
			else{
				queue.set("messages.update", null);
				HConH.saveMessageQueue(queue);
			}
		}
		PluginDescriptionFile pdfFile = this.getDescription();
		//Listeners and stuff
		getServer().getPluginManager().registerEvents((new HotelsListener(this)), this);//Firing event listener
		getCommand("Hotels").setExecutor(new HotelsCommandHandler(this));//Firing commands listener
		setupEconomy();
		//Economy and stuff
		if (!setupEconomy()){
			//If economy is turned on
			//but no vault is found it will warn the user
			String message = locale.getString("main.enable.noVault");
			if(message!=null){
				getLogger().severe(message);
			}
			else
				getLogger().severe("No Vault dependency found!");
		}

		//HotelsLoop stuff
		hotelsloop = new HotelsLoop(this);
		int minutes = this.getConfig().getInt("settings.hotelsLoopTimerMinutes");
		if(minutes>0)
			hotelsloop.runTaskTimer(this, 200, minutes*60*20);
		else
			hotelsloop.runTaskTimer(this, 200, 2*60*20);

		//Logging to console the correct enabling of Hotels
		String message = locale.getString("main.enable.success");
		if(message!=null){
			getLogger().info(locale.getString("main.enable.success").replaceAll("%pluginname%", pdfFile.getName()).replaceAll("%version%", pdfFile.getVersion()));
		}
		else
			getLogger().info(pdfFile.getName()+" v"+pdfFile.getVersion()+ " has been enabled correctly");
		//Metrics
		try {
			Metrics metrics = new Metrics(this);
			metrics.start();
		} catch (IOException e) {
			// Failed to submit the stats :-(
		}
	}
	@Override
	public void onDisable(){
		hotelsloop.cancel();

		PluginDescriptionFile pdfFile = this.getDescription();
		//Logging to console the disabling of Hotels
		String message = locale.getString("main.disable.success");
		if(message!=null){
			getLogger().info(locale.getString("main.disable.success").replaceAll("%pluginname%", pdfFile.getName()).replaceAll("%version%", pdfFile.getVersion()));
		}
		else
			getLogger().info(pdfFile.getName() + " v" + pdfFile.getVersion() + " has been disabled");
	}

	@Override
	public void onLoad(){
		setupConfig();
		setupEconomy();
	}

	//Setting up config files
	private void setupConfig(){
		HConH.setupConfigs(this);//Creates config file
	}

	//Setting up the economy
	private boolean setupEconomy()
	{
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}
		return (economy != null);
	}
}
