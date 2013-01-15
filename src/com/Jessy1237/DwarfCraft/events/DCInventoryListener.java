package com.Jessy1237.DwarfCraft.events;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.inventory.ItemStack;

import com.Jessy1237.DwarfCraft.DCPlayer;
import com.Jessy1237.DwarfCraft.DwarfCraft;
import com.Jessy1237.DwarfCraft.Effect;
import com.Jessy1237.DwarfCraft.EffectType;
import com.Jessy1237.DwarfCraft.Skill;

public class DCInventoryListener implements Listener {

	private DwarfCraft plugin;

	public DCInventoryListener(final DwarfCraft plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onFurnaceExtractEvent(FurnaceExtractEvent event) {
		if (plugin.getConfigManager().worldBlacklist) {
			for (World w : plugin.getConfigManager().worlds) {
				if (w != null) {
					if (event.getPlayer().getWorld() == w) {
						return;
					}
				}
			}
		}
		
		DCPlayer player = plugin.getDataManager().find(event.getPlayer());
		HashMap<Integer, Skill> skills = player.getSkills();
		Material item = event.getItemType();
		int amount = event.getItemAmount();

		for (Skill s : skills.values()) {
			for (Effect effect : s.getEffects()) {
				if (effect.getEffectType() == EffectType.SMELT && effect.checkInitiator(new ItemStack(item))) {
					item = effect.getOutput().getType();
					int newAmount = (int)(amount * effect.getEffectAmount(player));
					int i = 0;
					while(i != newAmount) {
						ItemStack itemstack;
						if((newAmount - i) < 64) {
							itemstack = new ItemStack(item, (newAmount - i));
							i = newAmount;
						} else {
							itemstack = new ItemStack(item, 64);
							i = i + 64;
						}
						player.getPlayer().getWorld().dropItemNaturally(player.getPlayer().getLocation(), itemstack);
					}
				}
			}
		}
	}
}
