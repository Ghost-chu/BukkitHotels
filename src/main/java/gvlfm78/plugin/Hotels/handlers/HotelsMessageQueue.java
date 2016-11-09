package gvlfm78.plugin.Hotels.handlers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class HotelsMessageQueue {

	private static YamlConfiguration queue = HotelsConfigHandler.getMessageQueue();

	public static HashMap<UUID, Set<String>> getMessages(MessageType type){ //Get all messages, regardless of player

		HashMap<UUID, Set<String>> allExpiryMessages = new HashMap<UUID, Set<String>>();

		ConfigurationSection section = queue.getConfigurationSection("messages." + type);

		if(section != null){
			Set<String> keys = section.getKeys(false);
			if(keys != null){

				for(String key : keys){ //Loop through all expiry messages
					UUID configUUID = UUID.fromString(queue.getString("messages." + type + "." + key + ".UUID"));

					//Loop through all expiry messages for that player
					ConfigurationSection messageSection = queue.getConfigurationSection("messages." + type + key + ".message"); //TODO get rid of key thing
					if(messageSection == null) continue;

					Set<String> messages = messageSection.getKeys(false);

					if(configUUID != null && messages != null && !messages.isEmpty())//If there actually are valid messages
						allExpiryMessages.put(configUUID, messages);

					queue.set("messages." + type + "." + key, null); //Remove the gathered messages from the queue
				}
			}
		}
		HotelsConfigHandler.saveMessageQueue(queue);
		return allExpiryMessages;
	}

	public static Set<String> getMessages(MessageType type, UUID uuid){ //Get messages for specific player
		Set<String> expiryMessages = new HashSet<String>();

		ConfigurationSection allExpiryMessages = queue.getConfigurationSection("messages." + type);

		if(allExpiryMessages != null){

			Set<String> keys = allExpiryMessages.getKeys(false);
			if(keys != null){

				for(String key : keys){
					String uuidString = queue.getString("messages." + type + "." + key + ".UUID");
					if(uuidString==null || uuidString.isEmpty()) continue;
					UUID configUUID = UUID.fromString(uuidString);
					if(uuid.equals(configUUID)){
						expiryMessages.add(queue.getString("messages." + type + "." + key + ".message"));

						//Remove found messages
						queue.set("messages." + type + "." + key, null);
						HotelsConfigHandler.saveMessageQueue(queue);
					}
				}

			}
		}
		return expiryMessages;
	}
	public static void addMessage(MessageType type, UUID uuid, String message){
		if(type==null || uuid==null || message==null) return;

		String path = "messages." + type + ".";
		queue.set(path + uuid.toString(), uuid.toString());

		path = path + "messages"; //This is all we're gonna need henceforth

		ConfigurationSection section = queue.getConfigurationSection(path); //Getting section with all messages

		if(section!=null){ //If there already are messages we must find the next available integer to place ours
			Set<String> messages = section.getKeys(false);
			if(messages != null && !messages.isEmpty()){

				int i = 0;
				while(queue.get(path + i) != null) //If this node already exists
					i++; //Go check the next one
				queue.set(path + i, message);
			}	
		}
	}
	public static void sendPlayerAllMessages(MessageType type, Player p){//Send all messages of specific type
		for(String mes : getMessages(type, p.getUniqueId()))
			if(mes!=null && !mes.isEmpty()) p.sendMessage(mes);
	}
	public static void sendPlayerAllMessages(Player p){//Send all messages of all types
		for(MessageType type : MessageType.values()){
			for(String mes : getMessages(type, p.getUniqueId()))
				if(mes!=null && !mes.isEmpty()) p.sendMessage(mes);
		}
	}
}