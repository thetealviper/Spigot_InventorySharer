package me.TheTealViper.enderbank;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.TheTealViper.enderbank.utils.ItemCreator;

public class CustomItemHandler implements Listener{
	
	public static EnderBank plugin;
	
	public static void setup(EnderBank plugin) {
		plugin.getServer().getPluginManager().registerEvents(new CustomItemHandler(), plugin);
		CustomItemHandler.plugin = plugin;
	}
	
	static ItemStack BankSeparator = null;
	public static ItemStack GetBankSeparator(){
		if(BankSeparator == null) {
			ItemStack customItem = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
	        ItemMeta meta = customItem.getItemMeta();
	        meta.setDisplayName(" ");
	        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
	        customItem.setItemMeta(meta);
	        BankSeparator = customItem;
		}
		return BankSeparator.clone();
	}
	
	static ItemStack NextPage = null;
	public static ItemStack GetNextPage(){
		if(NextPage == null) {
	        NextPage = ItemCreator.createItemFromConfiguration(EnderBank.plugin.getConfig().getConfigurationSection("GUI.Next_Page"));
		}
		return NextPage.clone();
	}
	
	static ItemStack BuyNextPage = null;
	public static ItemStack GetBuyNextPage(){
		if(BuyNextPage == null) {
	        BuyNextPage = ItemCreator.createItemFromConfiguration(EnderBank.plugin.getConfig().getConfigurationSection("GUI.Buy_Next_Page"));
		}
		return BuyNextPage.clone();
	}
	
	static ItemStack ConfirmBuyNextPage = null;
	public static ItemStack GetConfirmBuyNextPage(){
		if(ConfirmBuyNextPage == null) {
			ConfirmBuyNextPage = ItemCreator.createItemFromConfiguration(EnderBank.plugin.getConfig().getConfigurationSection("GUI.Confirm_Buy_Next_Page"));
		}
		return ConfirmBuyNextPage.clone();
	}
	
	static ItemStack PreviousPage = null;
	public static ItemStack GetPreviousPage(){
		if(PreviousPage == null) {
	        PreviousPage = ItemCreator.createItemFromConfiguration(EnderBank.plugin.getConfig().getConfigurationSection("GUI.Previous_Page"));
		}
		return PreviousPage.clone();
	}
	
	static ItemStack DumpEquipment = null;
	public static ItemStack GetDumpEquipment(){
		if(DumpEquipment == null) {
	        DumpEquipment = ItemCreator.createItemFromConfiguration(EnderBank.plugin.getConfig().getConfigurationSection("GUI.Dump_Equipment"));
		}
		return DumpEquipment.clone();
	}
	
	static ItemStack DumpItems = null;
	public static ItemStack GetDumpItems(){
		if(DumpItems == null) {
	        DumpItems = ItemCreator.createItemFromConfiguration(EnderBank.plugin.getConfig().getConfigurationSection("GUI.Dump_Items"));
		}
		return DumpItems.clone();
	}
	
	static ItemStack Search = null;
	public static ItemStack GetSearch(){
		if(Search == null) {
	        Search = ItemCreator.createItemFromConfiguration(EnderBank.plugin.getConfig().getConfigurationSection("GUI.Search"));
		}
		return Search.clone();
	}
	
	static ItemStack Separator = null;
	public static ItemStack GetSeparator(){
		if(Separator == null) {
	        Separator = ItemCreator.createItemFromConfiguration(EnderBank.plugin.getConfig().getConfigurationSection("GUI.Separator"));
		}
		return Separator.clone();
	}
	
	public static ItemStack formatLoreSyntax(ItemStack item, UUID uuid) {
		List<String> lore = item.hasItemMeta() && item.getItemMeta().hasLore() ? item.getItemMeta().getLore() : new ArrayList<String>();
		List<String> dummy = new ArrayList<String> (lore);
		BankStorage bank = BankStorage.getBank(uuid);
		for(int i = 0;i < dummy.size();i++) {
			lore.set(i, dummy.get(i).replace("%eb_pagecost%", BankStorage.getPageCost(bank.unlockedPages + 1) + ""));
		}
		ItemMeta meta = item.getItemMeta();
		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}
	
	
	
	
	
	
	
	
	//TODO
	//- add functions to cache all items here
	//- make sure itemcreator can pull from config properly
	//- make sure custom items work properly
	//- add in sound effect
	
	
	
	
	
	
	
	
	
	
	
}
