package gvlfm78.plugin.Hotels.managers;

import gvlfm78.plugin.Hotels.HotelsMain;
import gvlfm78.plugin.Hotels.handlers.HotelsCommandHandler;
import gvlfm78.plugin.Hotels.handlers.HotelsConfigHandler;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;

public class GameLoop extends BukkitRunnable {

	FilenameFilter SignFileFilter;
	HotelsMain plugin;
	public GameLoop(HotelsMain instance) {
		this.plugin = instance;
	}
	SignManager SM = new SignManager(plugin);
	HotelsFileFinder HFF = new HotelsFileFinder(plugin);
	WorldGuardManager WGM = new WorldGuardManager(plugin);
	HotelsConfigHandler HConH = new HotelsConfigHandler(plugin);

	//Prefix
	YamlConfiguration locale = HConH.getLocale();
	String prefix = (locale.getString("chat.prefix").replaceAll("(?i)&([a-fk-r0-9])", "\u00A7$1")+" ");

	public GameLoop(HotelsCommandHandler hotelsCommandHandler) {}

	@Override
	public void run() {
		//int list = new File("plugins//Hotels//Signs").listFiles().length;
		File dir = new File("plugins//Hotels//Signs");
		if(!(dir.exists()))
			dir.mkdir();

		ArrayList<String> fileslist = HFF.listFiles("plugins//Hotels//Signs");

		for(String x: fileslist){
			File file = new File("plugins//Hotels//Signs//"+x);
			if(file.getName().matches("^"+locale.getString("sign.reception")+"-.+-.+")){
				//It's a reception sign
				YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
				World world = Bukkit.getWorld(config.getString("Reception.location.world").trim());
				int locx = config.getInt("Reception.location.x");
				int locy = config.getInt("Reception.location.y");
				int locz = config.getInt("Reception.location.z");
				Block b = world.getBlockAt(locx,locy,locz);
				Location l = b.getLocation();
				if(SM.updateReceptionSign(l)==true){
					file.delete();
					b.setType(Material.AIR);
					plugin.getLogger().info(locale.getString("sign.delete.reception").replaceAll("%filename%", file.getName()));
				}
			}
			else{
				//Room sign
				YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
				String hotelName = config.getString("Sign.hotel");
				hotelName = hotelName.substring(0, 1).toUpperCase() + hotelName.substring(1);
				World world = Bukkit.getWorld(config.getString("Sign.location.world").trim());
				int roomNum = config.getInt("Sign.room");
				int locx = config.getInt("Sign.location.coords.x");
				int locy = config.getInt("Sign.location.coords.y");
				int locz = config.getInt("Sign.location.coords.z");
				Block signblock = world.getBlockAt(locx, locy, locz);

				if((signblock.getType().equals(Material.WALL_SIGN))||(signblock.getType().equals(Material.SIGN_POST))||(signblock.getType().equals(Material.SIGN))){
					Sign sign = (Sign) signblock.getState();
					if(hotelName.equalsIgnoreCase(ChatColor.stripColor(sign.getLine(0)))){
						String[] Line2parts = ChatColor.stripColor(sign.getLine(1)).split("\\s");
						int roomNumfromSign = Integer.valueOf(Line2parts[1].trim()); //Room Number
						if(roomNum==roomNumfromSign){
							//Room numbers match
							if(config.get("Sign.expiryDate")!=null){
								long expirydate = config.getLong("Sign.expiryDate");
								if(expirydate!=0){
									if(expirydate<System.currentTimeMillis()/1000/60){//If rent has expired
										String r = config.getString("Sign.region");
										ProtectedCuboidRegion region = (ProtectedCuboidRegion) WGM.getWorldGuard().getRegionManager(world).getRegion(r);
										if(config.getString("Sign.renter")!=null){
											OfflinePlayer p = Bukkit.getServer().getOfflinePlayer(UUID.fromString(config.getString("Sign.renter")));
											WGM.removeMember(p, region);

											//Removing friends
											List<String> stringList = config.getStringList("Sign.friends");
											for(String currentFriend : stringList){
												OfflinePlayer cf = Bukkit.getServer().getOfflinePlayer(UUID.fromString(currentFriend));
												WGM.removeMember(cf, region);
											}

											sign.setLine(3, "�a"+locale.getString("sign.vacant"));
											sign.update();
											plugin.getLogger().info(locale.getString("sign.rentExpiredConsole").replaceAll("%room%", String.valueOf(roomNum)).replaceAll("%hotel%", hotelName).replaceAll("%player%", p.getName()));
											if(p.isOnline()){
												Player op = Bukkit.getServer().getPlayer(UUID.fromString(config.getString("Sign.renter")));
												op.sendMessage(prefix+locale.getString("sign.rentExpiredPlayer").replaceAll("%room%", String.valueOf(roomNum)).replaceAll("%hotel%", hotelName).replaceAll("(?i)&([a-fk-r0-9])", "\u00A7$1"));
											}
											else{
												YamlConfiguration queue = HConH.getMessageQueue();
												if(!queue.contains("messages.expiry")){
													queue.createSection("messages.expiry");
													HConH.saveMessageQueue(queue);
												}
												Set<String> expiryMessages = queue.getConfigurationSection("messages.expiry").getKeys(false);
												int expiryMessagesSize = expiryMessages.size();
												String pathToPlace = "messages.expiry."+(expiryMessagesSize+1);
												queue.set(pathToPlace+".UUID", p.getUniqueId().toString());
												queue.set(pathToPlace+".message", locale.getString("sign.rentExpiredPlayer").replaceAll("%room%", String.valueOf(roomNum)).replaceAll("%hotel%", hotelName).replaceAll("(?i)&([a-fk-r0-9])", "\u00A7$1"));
												HConH.saveMessageQueue(queue);
											}
											config.set("Sign.renter", null);
											config.set("Sign.timeRentedAt", null);
											config.set("Sign.expiryDate", null);
											config.set("Sign.friends", null);
											try {
												config.save(file);
											} catch (IOException e) {
												e.printStackTrace();
											}
										}
										//Resetting time on sign to default
										sign.setLine(2, SM.TimeFormatter(config.getLong("Sign.time")));
										sign.update();
									}
									else{
										//Updating time remaining till expiry
										long expiryDate = config.getLong("Sign.expiryDate");
										long currentmins = System.currentTimeMillis()/1000/60;
										//Time remaining
										sign.setLine(2, SM.TimeFormatter(expiryDate-currentmins));
										sign.update();
									}
								}
							}
							else{
								config.set("Sign.renter", null);
								config.set("Sign.timeRentedAt", null);
								config.set("Sign.expiryDate", null);
								config.set("Sign.friends", null);
								try {
									config.save(file);
								} catch (IOException e) {
									e.printStackTrace();
								}
								sign.setLine(3, "�a"+locale.getString("sign.vacant"));
								sign.setLine(2, SM.TimeFormatter(config.getLong("Sign.time")));
								sign.update();
							}
						}
						else{
							file.delete();
							plugin.getLogger().info(locale.getString("sign.delete.roomNum").replaceAll("%filename%", file.getName()));}
					}
					else{
						file.delete();
						plugin.getLogger().info(locale.getString("sign.delete.hotelName").replaceAll("%filename%", file.getName()));}
				}
				else{
					file.delete();
					plugin.getLogger().info(locale.getString("sign.delete.location").replaceAll("%filename%", file.getName()));}
			}
		}
	}
}
