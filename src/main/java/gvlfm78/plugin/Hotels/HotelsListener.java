package gvlfm78.plugin.Hotels;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import gvlfm78.plugin.Hotels.handlers.HotelsMessageQueue;
import gvlfm78.plugin.Hotels.managers.Mes;
import gvlfm78.plugin.Hotels.managers.SignManager;
import gvlfm78.plugin.Hotels.trade.TradesHolder;

public class HotelsListener implements Listener {
	
	public HotelsListener(){}

	@EventHandler
	public void onSignPlace(SignChangeEvent e){
		//Player places sign, checking if it's a hotel sign
		Player p = e.getPlayer();
		//If sign is a hotels sign
		if(e.getLine(0).toLowerCase().contains("[hotels]")) {
			if(Mes.hasPerm(p,"hotels.sign.create")){
				//Sign lines
				String Line3 = ChatColor.stripColor(e.getLine(2)).trim();
				String Line4 = ChatColor.stripColor(e.getLine(3)).trim();

				if(Line3.isEmpty() && Line4.isEmpty()) //Reception sign?
					SignManager.placeReceptionSign(e);
				else //Room sign?
					SignManager.placeRoomSign(e);
			}
			else{
				//No permission
				Mes.mes(p, "chat.noPermission"); 
				e.setLine(0, ChatColor.DARK_RED + e.getLine(0));
			}
		}
	}

	@EventHandler
	public void onSignUse(PlayerInteractEvent e){
		//Player right clicks sign, checking if it's a hotel sign
		if(e.getAction() != Action.RIGHT_CLICK_BLOCK) return; //They Right clicked
		Block b = e.getClickedBlock();
		Material mat = b.getType();
		if(!mat.equals(Material.SIGN_POST) && !mat.equals(Material.WALL_SIGN)) return; //If block is sign
		Player p = e.getPlayer();
		//Permission check
		if(Mes.hasPerm(p, "hotels.sign.use"))
			SignManager.useRoomSign(e);
		else Mes.mes(p, "chat.noPermission"); 
	}

	@EventHandler
	public void onSignBreak(BlockBreakEvent e){
		//Player broke a sign, checking if it's a hotel sign
		Block b = e.getBlock();
		Material mat = b.getType();
		if(mat.equals(Material.SIGN_POST) || mat.equals(Material.WALL_SIGN))
			SignManager.breakRoomSign(e);
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e){
		final Player p = e.getPlayer();
		//Send player all queued up messages for them
		HotelsMessageQueue.sendPlayerAllMessages(p);
	}
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e){
		//Removing them from Trades hashmap if they were in it
		Player p = e.getPlayer();
		TradesHolder.removeFromAll(p);

		//Exiting them from creation mode
		if(HotelsCreationMode.isInCreationMode(p.getUniqueId())){
			Mes.mes(p, "chat.commands.creationMode.exit");
			HotelsCreationMode.loadInventory(p);
		}
	}
	//When a player tries to drop an item/block
	@EventHandler
	public void avoidDrop(PlayerDropItemEvent e) {
		Player p = e.getPlayer();
		if(HotelsCreationMode.isInCreationMode(p.getUniqueId())){
			e.setCancelled(true);
			Mes.mes(p, "chat.creationMode.deniedAction");
		}
	}
	//When a player tries to pickup an item/block
	@EventHandler
	public void avoidPickup(PlayerPickupItemEvent e) {
		Player p = e.getPlayer();
		if(HotelsCreationMode.isInCreationMode(p.getUniqueId())){
			e.setCancelled(true);
			Mes.mes(p, "chat.creationMode.deniedAction");
		}
	}
	@EventHandler
	public void avoidChestInteraction(InventoryClickEvent e){
		Player p = (Player) e.getWhoClicked();
		if(!Mes.hasPerm(p, "hotels.createmode.admin")) return;
		if(HotelsCreationMode.isInCreationMode(p.getUniqueId())){
			e.setCancelled(true);
			Mes.mes(p, "chat.creationMode.deniedAction");
		}
	}
}