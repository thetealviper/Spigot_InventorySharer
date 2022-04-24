package me.TheTealViper.inventorysharer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.Inventory;

import me.TheTealViper.itemsharer.utils.UtilityEquippedJavaPlugin;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class InventorySharer extends UtilityEquippedJavaPlugin implements Listener {
	RandomString RANDOMSTRING = new RandomString(15);
	List<ViewableInventory> invList = new ArrayList<ViewableInventory>();
	Map<Player, ViewableInventory> pendingOpenInvDatabase = new HashMap<Player, ViewableInventory>();
	Map<Player, ViewableInventory> openInvDatabase = new HashMap<Player, ViewableInventory>();
	
	public void onEnable(){
		StartupPlugin(this, "63276");
	}
	public void onDisable(){
		//Close open inventories because reloads will wipe open inventory database while keeping the inventories open by default. Players could abuse server reloads to pull items after.
		for(Player p : Bukkit.getOnlinePlayers()) {
			if(openInvDatabase.containsKey(p))
				p.closeInventory();
		}
	}
	
	@EventHandler
	public void onPreCommand(PlayerCommandPreprocessEvent e) {
		if(e.getMessage().startsWith("/[inv]")) {
			e.setCancelled(true);
			if(e.getPlayer().hasPermission("inventorysharer.open")) {
				boolean foundInventory = false;
				for(ViewableInventory vi : invList) {
					if(e.getMessage().replace("/[inv]", "").equals(vi.ss)) {
						pendingOpenInvDatabase.put(e.getPlayer(), vi);
						e.getPlayer().openInventory(vi.inv);
						foundInventory = true;
						break;
					}
				}
				if(!foundInventory) {
					e.getPlayer().sendMessage("That inventory has expired.");
				}
			}else {
				e.getPlayer().sendMessage("You are missing the permissions to do that.");
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onChat(AsyncPlayerChatEvent e) {
		if(e.isCancelled())
			return;
		if(e.getMessage().contains("[inv]")) {
			if(e.getPlayer().hasPermission("inventorysharer.create")) {
				//Handle chat stuff
				String formatted = String.format(e.getFormat(), e.getPlayer().getDisplayName(), e.getMessage());
				TextComponent cSpecial = new TextComponent("[inv]");
				String secretString = RANDOMSTRING.nextString();
				cSpecial.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/[inv]" + secretString));
				cSpecial.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click me to view inventory.").create()));
				List<String> brokenMessage = new ArrayList<String>(Arrays.asList(formatted.split("(\\[inv\\])")));
				if(formatted.endsWith("[inv]"))
					brokenMessage.add(""); //If the message ends in [url] the regex above doesn't capture the end as a unique string. This manually adds it.
				TextComponent c = new TextComponent(brokenMessage.get(0)); //Before segment
				for(int i = 1;i < brokenMessage.size();i++) {
					//Allow numerous tags in singular message?
					if(i == 1 //Always allow the first tag
							|| (i != 1 && !getConfig().getBoolean("Disallow_Numerous_Inv_Per_Message"))) //If not the first tag, allow numerous?
						c.addExtra(cSpecial); //Customized [inv] segment
					else {
						//Duplicate Space Trimming Below if no duplicates
						if(brokenMessage.get(i-1).endsWith(" ") && brokenMessage.get(i).startsWith(" "))
							brokenMessage.set(i, brokenMessage.get(i).substring(1));
					}
					c.addExtra(brokenMessage.get(i)); //After segment
				}
				e.setCancelled(true);
				for(Player p : Bukkit.getOnlinePlayers()) {
					p.spigot().sendMessage(c);
				}
				
				//Handle saving inventory stuff
				Inventory inv = Bukkit.createInventory(null, 45, e.getPlayer().getDisplayName() + "'s [inv]");
//				Bukkit.broadcastMessage("" + e.getPlayer().getInventory().getContents().length);
				inv.setContents(e.getPlayer().getInventory().getContents());
				ViewableInventory vi = new ViewableInventory(secretString, inv);
				invList.add(vi);
				if(invList.size() > getConfig().getInt("Cached_Inventories"))
					invList.remove(0);
				Bukkit.getServer().getLogger().info("[InventorySharer Debug] " + e.getPlayer().getName() + " has generated viewable inventory: " + vi.ss);
			}
		}
	}

	@EventHandler
	public void onInvOpen(InventoryOpenEvent e) {
		if(pendingOpenInvDatabase.containsKey(e.getPlayer())) {
			Player p = (Player) e.getPlayer();
			ViewableInventory viBuffer = pendingOpenInvDatabase.get(p); //Use a buffer so we don't accidentally have value in both databases
			pendingOpenInvDatabase.remove(p);
			openInvDatabase.put(p, viBuffer);
		}
	}
	@EventHandler
	public void onInvClose(InventoryCloseEvent e) {
		if(openInvDatabase.containsKey(e.getPlayer())) {
			openInvDatabase.remove(e.getPlayer());
		}
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		if(e.isCancelled())
			return;
		if(openInvDatabase.containsKey(e.getWhoClicked())) {
			e.setCancelled(true);
			if(e.getClick().equals(ClickType.SWAP_OFFHAND))
				e.getWhoClicked().getInventory().setItemInOffHand(e.getWhoClicked().getInventory().getItemInOffHand()); //This is necessary to fix a visual bug showing a phantom item which isn't real client side
		}
	}
	
}
