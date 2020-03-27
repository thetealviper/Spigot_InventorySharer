package me.TheTealViper.enderbank;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import me.TheTealViper.enderbank.utils.EnableShit;
import me.TheTealViper.enderbank.utils.VersionType;
import me.TheTealViper.enderbank.utils.ViperStringUtils;
import net.milkbowl.vault.economy.Economy;

public class EnderBank extends JavaPlugin implements Listener {
	//general
	public static EnderBank plugin;
	public static VersionType version;
	public static String notificationString = ChatColor.BOLD + "[" + ChatColor.AQUA + ChatColor.BOLD + "!" + ChatColor.WHITE + ChatColor.BOLD + "]" + ChatColor.RESET
			, questionString = ChatColor.BOLD + "[" + ChatColor.AQUA + ChatColor.BOLD + "?" + ChatColor.WHITE + ChatColor.BOLD + "]" + ChatColor.RESET;
	
	//Chat Queue (for asking which tracker you'd like to add)
	public static Map<Player, List<String>> chatHandlerQueue = new HashMap<Player, List<String>>();
	
	//plugin specific variables
	public static ItemStack confirmBuy;
	public static Map<Player, String> pendingResponseDatabase = new HashMap<Player, String>();
	public static List<Material> equipmentTypes = new ArrayList<Material>();
	private static Economy econ = null;
	
	public void onEnable() {
		//	Do cleanup in case this is a reload
		Bukkit.getServer().getScheduler().cancelTasks(this);
		
		//	Register this plugin
		EnableShit.handleOnEnable(this, this, "-1");
		plugin = this;

		//Setup modules
		BankStorage.setup(this);
		CustomItemHandler.setup(this);
		
		//Load values from config
		saveDefaultConfig();
		
		//Set initial values
		ItemMeta meta = Bukkit.getItemFactory().getItemMeta(Material.STICK);
		confirmBuy = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
		meta = Bukkit.getItemFactory().getItemMeta(Material.STICK);
		meta.setDisplayName("Confirm Purchase");
		List<String> lore = new ArrayList<String>();
		lore.add(ChatColor.RESET + "" + ChatColor.ITALIC + "Click again to confirm purchase!");
		meta.setLore(lore);
		confirmBuy.setItemMeta(meta);
		
		equipmentTypes.add(Material.CHAINMAIL_BOOTS);
		equipmentTypes.add(Material.CHAINMAIL_CHESTPLATE);
		equipmentTypes.add(Material.CHAINMAIL_HELMET);
		equipmentTypes.add(Material.CHAINMAIL_LEGGINGS);
		equipmentTypes.add(Material.LEATHER_BOOTS);
		equipmentTypes.add(Material.LEATHER_CHESTPLATE);
		equipmentTypes.add(Material.LEATHER_HELMET);
		equipmentTypes.add(Material.LEATHER_LEGGINGS);
		equipmentTypes.add(Material.IRON_BOOTS);
		equipmentTypes.add(Material.IRON_CHESTPLATE);
		equipmentTypes.add(Material.IRON_HELMET);
		equipmentTypes.add(Material.IRON_LEGGINGS);
		equipmentTypes.add(Material.GOLDEN_BOOTS);
		equipmentTypes.add(Material.GOLDEN_CHESTPLATE);
		equipmentTypes.add(Material.GOLDEN_HELMET);
		equipmentTypes.add(Material.GOLDEN_LEGGINGS);
		equipmentTypes.add(Material.DIAMOND_BOOTS);
		equipmentTypes.add(Material.DIAMOND_CHESTPLATE);
		equipmentTypes.add(Material.DIAMOND_HELMET);
		equipmentTypes.add(Material.DIAMOND_LEGGINGS);
		
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp != null)
            econ = rsp.getProvider();
	}
	
	public void onDisable() {
		
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if(sender instanceof Player){
            Player p = (Player) sender;
            boolean explain = false;
            boolean warnmissingperms = false;
            if(args.length == 0) {
            	if(p.hasPermission("enderbank.staff")) {
            		explain = true;
            	}else {
            		warnmissingperms = true;
            	}
            } else if(args.length == 1){
            	if(p.hasPermission("enderbank.staff")){
            		explain = true;
            	}else
            		warnmissingperms = true;
            }else if(args.length == 2){
            	if(args[0].equalsIgnoreCase("open")){
            		if(p.hasPermission("enderbank.staff")) {
                		String oPlayerName = args[1];
                		@SuppressWarnings("deprecation")
						OfflinePlayer oPlayerOffline = Bukkit.getOfflinePlayer(oPlayerName);
                		if(oPlayerOffline.isOnline()) {
                			Player oPlayer = oPlayerOffline.getPlayer();
                			BankStorage bank = BankStorage.getBank(oPlayer);
                			bank.openPage(1, p);
                		}else {
                			p.sendMessage("That player is not online.");
                		}
                	}else {
                		warnmissingperms = true;
                	}
            	}else
            		explain = true;
            }else if(args.length > 2) {
            	if(p.hasPermission("enderbank.staff")) {
            		explain = true;
            	}else {
            		warnmissingperms = true;
            	}
            }
            if(warnmissingperms) {
            	p.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "You are missing permissions!");
            }
            if(!warnmissingperms && explain){
            	p.sendMessage("EnderBank Commands");
        		p.sendMessage("/enderbank open <name>" + ChatColor.GRAY + " - Opens online player's inventory.");
            }
        }else{
        	//Not a player
        	sender.sendMessage("Commands can only be typed by in game players for this plugin!");
        }
        
        return true;
	}
	
	public void log(String s) {
		getServer().getConsoleSender().sendMessage(ViperStringUtils.makeColors(s));
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		Player opener = (Player) e.getWhoClicked();
		if(!BankStorage.openBankDatabase.containsKey(opener))
			return;
		BankStorage bank = BankStorage.openBankDatabase.get(opener);
		
		if(e.getView().getTitle().equals("Bank Search") && e.getClickedInventory() != null && e.getClickedInventory().equals(e.getView().getTopInventory())) {
			if(e.getSlot() >= bank.itemIdentifiers.size()) {
				e.setCancelled(true);
			}
		}else if(e.getSlot() != -1 && e.getClickedInventory() != null && e.getView().getTitle().contains("'s Bank [Pg. ")) {
			if(e.getSlot() == 7 || e.getSlot() == 16 || e.getSlot() == 25 || e.getSlot() == 26 || e.getSlot() == 34 || e.getSlot() == 43 || e.getSlot() == 52) {
				e.setCancelled(true);
			}else if(e.getSlot() == 8) { //next page
				e.setCancelled(true);
				if(pendingResponseDatabase.containsKey(opener) && pendingResponseDatabase.get(opener).equals("buypage")) { //If responding to confirm purchase
					//Confirm if have the funds
					if(econ.has(opener.getName(), BankStorage.getPageCost(bank.unlockedPages + 1))) {
						econ.withdrawPlayer(opener.getName(), BankStorage.getPageCost(bank.unlockedPages + 1));
						pendingResponseDatabase.remove(opener);
						bank.unlockedPages = bank.pf.getInt("unlockedPages") + 1;
						bank.savePage(bank.lastOpenedPage, e.getInventory());
						bank.openPage(bank.lastOpenedPage + 1, opener);
					}else {
						opener.sendMessage(EnderBank.notificationString + " You don't have enough money!");
					}
				}else if(bank.lastOpenedPage == bank.unlockedPages) { //If clicking buy ask to confirm
					e.getInventory().setItem(8, confirmBuy.clone());
					pendingResponseDatabase.put(opener, "buypage");
				}else{ //If clicking next page
					pendingResponseDatabase.remove(opener);
					bank.savePage(bank.lastOpenedPage, e.getInventory());
					bank.openPage(bank.lastOpenedPage + 1, opener);
				}
				e.setCancelled(true);
			}else if(e.getSlot() == 17) { //prev page
				e.setCancelled(true);
				if(bank.lastOpenedPage != 1) {
					pendingResponseDatabase.remove(opener);
					bank.savePage(bank.lastOpenedPage, e.getInventory());
					bank.openPage(bank.lastOpenedPage - 1, opener);
				}
			}else if(e.getSlot() == 26) {
				e.setCancelled(true);
			}else if(e.getSlot() == 35) {
				e.setCancelled(true);
				bank.savePage(bank.lastOpenedPage, e.getInventory());
				for(int j = 9;j < 13;j++) {
					if(opener.getInventory().getItem(j) != null) {
						//Check if rune filler
//							boolean currentIsTome = false;
//							if(!p.getInventory().getItem(j).getType().toString().equals("AIR")) {
//								if(p.getInventory().getItem(j).hasItemMeta()) {
//									if(p.getInventory().getItem(j).getItemMeta().hasDisplayName()) {
//										if(p.getInventory().getItem(j).getItemMeta().getDisplayName().contains("Tome")) {
//											currentIsTome = true;
//										}else {
//
//										}
//									}else {
//
//									}
//								}else {
//
//								}
//							}
//							
//							if(currentIsTome) {
//								bank.attemptToAddToBank(j);
//								p.getInventory().setItem(j, CustomItemHandler.GetRelicFiller());
//							}
					}
				}
				for(int j = 36;j < 40;j++) {
					if(opener.getInventory().getItem(j) != null) {
						bank.attemptToAddToBank(j, opener);
					}
				}
				bank.openPage(bank.lastOpenedPage, opener);
			}else if(e.getSlot() == 44) { //Dump all items
				e.setCancelled(true);
				bank.savePage(bank.lastOpenedPage, e.getInventory());
				if(e.getAction().equals(InventoryAction.PICKUP_ALL)) { //Encluding hotbar
					for(int j = 9;j < 36;j++) {
						if(opener.getInventory().getItem(j) != null) {
							bank.attemptToAddToBank(j, opener);
						}
					}
				}else if(e.getAction().equals(InventoryAction.PICKUP_HALF)) { //Including Hotbar
					for(int j = 0;j < 9;j++) {
						if(opener.getInventory().getItem(j) != null) {
							bank.attemptToAddToBank(j, opener);
						}
					}
					for(int j = 9;j < 36;j++) {
						if(opener.getInventory().getItem(j) != null) {
							bank.attemptToAddToBank(j, opener);
						}
					}
					if(opener.getInventory().getItem(40) != null) {
						bank.attemptToAddToBank(40, opener);
					}
				}
				bank.openPage(bank.lastOpenedPage, opener);
			}else if(e.getSlot() == 53) {
				e.setCancelled(true);
				BankStorage.searchDatabase.put(opener, bank);
				opener.closeInventory();
				
				if(!chatHandlerQueue.containsKey(opener))
					chatHandlerQueue.put(opener, new ArrayList<String>());
				List<String> queue = chatHandlerQueue.get(opener);
				queue.add("banksearch");
				chatHandlerQueue.put(opener, queue);
				
				opener.sendMessage(EnderBank.questionString + " Please search in chat."
						+ "\nType 'cancel' to cancel.");
			}else{
				e.setCancelled(false);
			}
		}
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent e) {
		Player opener = (Player) e.getPlayer();
		if(!BankStorage.openBankDatabase.containsKey(opener))
			return;
		BankStorage bank = BankStorage.openBankDatabase.get(opener);
		
		if(e.getView().getTitle().equals("Bank Search")) {
			Inventory inv = e.getInventory();
			for(int i = 0;i < inv.getSize();i++) {
				if(i <= bank.itemIdentifiers.size() - 1) {
					//Update item in bank
					bank.items.set(bank.itemIdentifiers.get(i), inv.getItem(i));
				}
			}
		}else if(bank.lastOpenedInventory != null && e.getInventory().equals(bank.lastOpenedInventory)) {
			int page = bank.lastOpenedPage;
			bank.savePage(page, e.getInventory());
		}
		
		if(pendingResponseDatabase.containsKey(opener))
			pendingResponseDatabase.remove(opener);
	}
	
	@EventHandler
	public void onInventoryOpen(InventoryOpenEvent e) {
		if(e.getInventory().getType().equals(InventoryType.ENDER_CHEST)) {
			e.setCancelled(true);
			Player p = (Player) e.getPlayer();
			BankStorage bank = BankStorage.getBank(p);
			bank.openPage(1, p);
		}
	}
	
	@EventHandler
	public void onChat(AsyncPlayerChatEvent e) {
		Player p = e.getPlayer();
		if(!chatHandlerQueue.containsKey(p))
			return;
		List<String> queue = chatHandlerQueue.get(p);
		if(queue.size() == 0)
			return;
		String handler = queue.get(queue.size() - 1);
		if(e.getMessage().equalsIgnoreCase("cancel")) {
			queue.remove(queue.size() - 1);
			e.setCancelled(true);
			p.sendMessage(EnderBank.notificationString + " Cancelled successfully.");
			return;
		}
		if(handler.equals("banksearch")) {
			queue.remove(queue.size() - 1);
			e.setCancelled(true);
			String search = e.getMessage();
			BankStorage bank = BankStorage.searchDatabase.get(p);
			bank.openSearch(search, p);
			BankStorage.searchDatabase.remove(p);
		}
	}
	
}
