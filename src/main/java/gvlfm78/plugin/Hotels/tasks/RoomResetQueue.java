/**
 * 
 */
package gvlfm78.plugin.Hotels.tasks;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.world.DataException;
import gvlfm78.plugin.Hotels.HotelsMain;
import gvlfm78.plugin.Hotels.Room;
import gvlfm78.plugin.Hotels.managers.Mes;
import org.bukkit.Bukkit;

import java.io.IOException;

/**
 * Queue room resets and stagger them to minimise lag
 */
public class RoomResetQueue {

	private static HotelsMain PLUGIN = HotelsMain.getHotels();
	private static long lastAdding;
	
	public static void add(final Room room){
		
		long delay = PLUGIN.getConfig().getLong("roomResetDelay", 5L);
		long currentTime = System.currentTimeMillis();
		
		//This makes it so there is a constant minimum time difference between tasks
		long secondsUntilLastTaskIsExecuted = delay - (currentTime - lastAdding) / 1000;

		if(secondsUntilLastTaskIsExecuted < 0) secondsUntilLastTaskIsExecuted = 0;
		
		Bukkit.getScheduler().runTaskLater(PLUGIN, () -> {
            try {
                Mes.debug("Resetting room " + room.getNum() + " of Hotel " + room.getHotel().getName());
                room.reset();
            } catch (IOException | WorldEditException | DataException e) {
                e.printStackTrace();
            }
        }, (20 * (delay + secondsUntilLastTaskIsExecuted)) );

		lastAdding = currentTime;
	}
}
