package me.TheTealViper.inventorysharer;

import org.bukkit.inventory.Inventory;

public class ViewableInventory {
	public String ss = "";
	public Inventory inv = null;
	
	public ViewableInventory(String secretString, Inventory inventory) {
		ss = secretString;
		inv = inventory;
	}
}