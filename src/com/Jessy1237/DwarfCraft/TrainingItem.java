package com.Jessy1237.DwarfCraft;

/**
 * Original Authors: smartaleq, LexManos and RCarretta
 */

import org.bukkit.inventory.ItemStack;

public class TrainingItem {

	public final ItemStack Item;
	public final double Base;
	public final int Max;
	
	public TrainingItem(ItemStack item, double base, int max){
		Item = item;
		Base = base;
		Max = max;
	}
}
