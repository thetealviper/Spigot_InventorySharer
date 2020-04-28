package me.TheTealViper.enderbank;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.TheTealViper.enderbank.utils.PluginFile;

public class BankStorage {
	public static Map<UUID, BankStorage> bankDatabase = new HashMap<UUID, BankStorage>(); //	This links players to THEIR own bank
	public static Map<Player, BankStorage> openBankDatabase = new HashMap<Player, BankStorage>(); //	This links players to the bank they are viewing
	public static Map<Player, BankStorage> searchDatabase = new HashMap<Player, BankStorage>(); //	This links players to the last bank they tried to search
	public static EnderBank plugin;
	
	public List<ItemStack> items;
	public int unlockedPages;
	public PluginFile pf;
	public Inventory lastOpenedInventory;
	public int lastOpenedPage;
	public List<Integer> itemIdentifiers;
	public UUID bankOwnerUUID;
	
	public static void setup(EnderBank plugin) {
		BankStorage.plugin = plugin;
	}
	
	public static BankStorage getBank(UUID bankOwner) {
		if(bankDatabase.containsKey(bankOwner))
			return bankDatabase.get(bankOwner);
		else
			return new BankStorage(bankOwner);
	}
	
	public BankStorage(UUID bankOwnerUUID) {
		bankDatabase.put(bankOwnerUUID, this);
		this.bankOwnerUUID = bankOwnerUUID;
		pf = new PluginFile(plugin, "banks/banks." + bankOwnerUUID.toString() + ".yml");
		
		//Load in defaults if not saved yet
		if(!pf.contains("unlockedPages")) {
			for(int i = 0;i < 42;i++) {
				pf.set("inventory." + i, new ItemStack(Material.AIR));
			}
			pf.set("unlockedPages", 1);
			pf.save();
		}
		
		//Load in items
		items = new ArrayList<ItemStack>();
		ConfigurationSection sec = pf.getConfigurationSection("inventory");
		for(int i = 0;i < sec.getKeys(false).size();i++) {
			items.add(sec.getItemStack(i + ""));
		}
		
		//Blah
		this.unlockedPages = pf.getInt("unlockedPages");
	}
	
	public void openPage(int page, Player opener) {
		if(page == 1 && EnderBank.plugin.getConfig().getBoolean("Enable_Open_Bank_Noise")) {
			if(EnderBank.plugin.getConfig().getBoolean("Open_Bank_Noise_Global")) {
				opener.getWorld().playSound(opener.getLocation(), Sound.valueOf(EnderBank.plugin.getConfig().getString("Open_Bank_Noise")), 1, 1);
			}else {
				opener.playSound(opener.getLocation(), Sound.valueOf(EnderBank.plugin.getConfig().getString("Open_Bank_Noise")), 1, 1);
			}
		}
		
		openBankDatabase.put(opener, this);
		Inventory inv = Bukkit.createInventory(null, 54, Bukkit.getOfflinePlayer(bankOwnerUUID).getName() + "'s Bank [Pg. " + page + "]");
		
		int startingIndex = (page - 1) * 42;
//		int endingIndex = page * 42 - 1;
		for(int row = 0;row < 6;row++) {
			for(int column = 0;column < 7;column++) {
				if(items.size() - 1 < startingIndex + column + 7 * row) { //If open a page whose inventory isn't in config yet
					ItemStack air = new ItemStack(Material.AIR);
					for(int i = 0; i < 42;i++) {
						items.add(items.size() - 1, air);
					}
				}
				
				inv.setItem(row * 9 + column, items.get(startingIndex + column + 7 * row));
			}
		}
		
		
		ItemStack NextPage = null;
		if(unlockedPages > page) {
			NextPage = CustomItemHandler.GetNextPage();
		}else {
			NextPage = CustomItemHandler.formatLoreSyntax(CustomItemHandler.GetBuyNextPage(), bankOwnerUUID);
		}
		inv.setItem(8, NextPage);
		
		ItemStack previousPage = CustomItemHandler.GetPreviousPage();
		inv.setItem(17, previousPage);
		
		ItemStack dumpEquipment = CustomItemHandler.GetDumpEquipment();
		inv.setItem(35, dumpEquipment);
		
		ItemStack dumpItems = CustomItemHandler.GetDumpItems();
		inv.setItem(44, dumpItems);
		
		ItemStack search = CustomItemHandler.GetSearch();
		inv.setItem(53, search);
		
		for(int row = 0;row < 6;row++) {
			inv.setItem(7 + 9 * row, CustomItemHandler.GetBankSeparator());
		}
		inv.setItem(26, CustomItemHandler.GetBankSeparator());
		
		lastOpenedInventory = inv;
		lastOpenedPage = page;
		opener.openInventory(inv);
	}
	
	public void savePage(int page, Inventory inv) {
		int startingIndex = (page - 1) * 42;
		
		for(int row = 0;row < 6;row++) {
			for(int column = 0;column < 7;column++) {
				ItemStack item = inv.getItem(row * 9 + column);
				if(item == null)
					item = new ItemStack(Material.AIR);
				items.set(startingIndex + column + 7 * row, item);
			}
		}
		
		save();
	}
	
	public void save() {
		pf.set("inventory", null);
		for(int i = 0;i < items.size();i++) {
			pf.set("inventory." + i, items.get(i));
		}
		pf.set("unlockedPages", unlockedPages);
		pf.save();
	}
	
	public static int getPageCost(int page) {
		int starter = plugin.getConfig().getInt("Default_Page_Price");
		double multiplier = plugin.getConfig().getDouble("Page_Price_Multiplier");
		int addition = plugin.getConfig().getInt("Page_Price_Addition");
		int partOne = (int) (starter * Math.pow(multiplier, page - 2));
		int price = partOne + (addition * (page - 2));
		return price;
	}
	
	public void attemptToAddToBank(int playerInvSlotNumber, Player opener) {
		ItemStack pItem = opener.getInventory().getItem(playerInvSlotNumber);
		int pAmount = pItem.getAmount();
		int maxAmount = pItem.getMaxStackSize();
		boolean replaced = false;
		for(int i = 0;i < items.size();i++) {
			if(replaced)
				continue;
			
			//First check for same items
			if(items.get(i).isSimilar(pItem)) { //If there is empty space in the bank
				ItemStack bItem = items.get(i);
				int bAmount = bItem.getAmount();
				int originalBAmount = bAmount;
				if(bAmount < maxAmount) { //If there is room to merge stacks
					bAmount += pAmount;
					bAmount = bAmount > maxAmount ? maxAmount : bAmount;
					bItem.setAmount(bAmount);
					items.set(i, bItem);
					pAmount -= maxAmount - originalBAmount;
				}
				
				if(pAmount == 0)
					replaced = true;
			}
			
			//Then check for air
			if(items.get(i) == null || items.get(i).getType().equals(Material.AIR)) { //If there is empty space in the bank
				ItemStack bItem = pItem.clone();
				bItem.setAmount(pAmount);
				items.set(i, bItem);
				pAmount = 0;
				replaced = true;
			}
		}
		
		pItem.setAmount(pAmount);
		opener.getInventory().setItem(playerInvSlotNumber, pItem);
		if(playerInvSlotNumber == 40 && pAmount == 0)
			opener.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
	}
	
	public void openSearch(String search, Player opener) {
		//Load up inventory
		this.itemIdentifiers = new ArrayList<Integer>();
		for(int i = 0;i < items.size();i++) {
			ItemStack item = items.get(i);
			
			if(itemIdentifiers.size() < 54) {
				boolean found = false;
				
				//Check item custom names first
				if(item.hasItemMeta() && item.getItemMeta().hasDisplayName() && item.getItemMeta().getDisplayName().toLowerCase().contains(search.toLowerCase())) {
					found = true;
					itemIdentifiers.add(i);
				}
				
				//Check item default names next
				if(item.getType().toString().replace("_", " ").toLowerCase().contains(search.toLowerCase()) && !found) {
					found = true;
					itemIdentifiers.add(i);
				}
				
				//Check lores next
				if(item.hasItemMeta() && item.getItemMeta().hasLore() && !found) {
					boolean loreFound = false;
					for(String s : item.getItemMeta().getLore()) {
						if(loreFound)
							continue;
						if(s.toLowerCase().contains(search.toLowerCase())) {
							loreFound = true;
						}
					}
					
					if(loreFound) {
						itemIdentifiers.add(i);
					}
				}
			}
		}
		
		//Display inventory
		Inventory inv = Bukkit.createInventory(null, 54, "Bank Search");
		for(int i = 0;i < 54;i++) {
			if(i < itemIdentifiers.size()) {
				inv.setItem(i, items.get(itemIdentifiers.get(i)));
			}else {
				ItemStack filler = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
				ItemMeta meta = Bukkit.getItemFactory().getItemMeta(Material.STICK);
				meta.setDisplayName(" ");
				filler.setItemMeta(meta);
				inv.setItem(i, filler);
			}
		}
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {public void run() {
			opener.closeInventory();
			opener.openInventory(inv);
		}}, 0);
	}
	
}
