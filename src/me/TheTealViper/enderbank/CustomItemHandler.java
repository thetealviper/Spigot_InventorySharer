package me.TheTealViper.enderbank;

import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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
		return BankSeparator;
	}
	
}
