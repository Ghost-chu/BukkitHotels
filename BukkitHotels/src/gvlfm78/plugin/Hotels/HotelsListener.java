package gvlfm78.plugin.Hotels;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class HotelsListener implements Listener {
	public final HashMap<Player, ArrayList<Block>> hashmapPlayerName = new HashMap<Player, ArrayList<Block>>();
	
	@EventHandler
	public void onSignChange(SignChangeEvent e) {
        Player p = e.getPlayer();
        if(e.getLine(0).contains("[Hotels]")) {
        	String Line2 = e.getLine(1);
  			String Line3 = e.getLine(2);
  			String Line4 = e.getLine(3);
        		if (!(Line2.isEmpty())) {
        			if(Integer.valueOf(Line3).equals(Line3)) {
        				if(Line4.contains(":")) {
        					//Successful Sign
        					e.setLine(0, Line2);
        					e.setLine(1, "�2Room " + Line2);
        					
        					//TODO Need to split Line 4 and Set line 3 as the first half of Line 4.
        					e.setLine(2,"");
        					p.sendMessage(ChatColor.DARK_GREEN + "Hotel sign has been successfully crated!");
        					
        					//TODO Add to config
        					
        			} else {
        				p.sendMessage(ChatColor.DARK_RED + "Line 4 must contain a separator");    				
        				e.setLine(0, "�4[Hotels]");
        			}
        		} else {
        			p.sendMessage(ChatColor.DARK_RED + "Line 3 must be an integer!");        			
    				e.setLine(0, "�4[Hotels]");
        		}
        	} else {
        		p.sendMessage(ChatColor.DARK_RED + "Line 2 is empty!");        		
				e.setLine(0, "�4[Hotels]");
        	}
        }
    }
	
	@EventHandler
	public void onSignUse(PlayerInteractEvent e) {
		
	if (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_BLOCK) {
		if (e.getClickedBlock().getType() == Material.SIGN_POST || e.getClickedBlock().getType() == Material.WALL_SIGN) {
			Sign s = (Sign) e.getClickedBlock().getState();
			
			//TODO We need to get from a YAML File. So on creating one lets make a Hashmap or list and add into Config!
			if (s.getLine(0).equalsIgnoreCase("") && s.getLine(1).equalsIgnoreCase("")) {
				Player p = e.getPlayer();
				
				//TODO get player name and add his name to config under the room?
				}	
			}
		}
	}	
		}