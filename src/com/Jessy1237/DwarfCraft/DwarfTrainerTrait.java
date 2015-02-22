package com.Jessy1237.DwarfCraft;

import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.trait.Trait;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;

public class DwarfTrainerTrait extends Trait{

	private DwarfCraft plugin;
	
	public DwarfTrainerTrait() {
		super("DwarfTrainer");
		this.plugin = (DwarfCraft) Bukkit.getServer().getPluginManager().getPlugin("DwarfCraft");
	}
	
	@EventHandler
	public void onNPCLeftClick(NPCLeftClickEvent event) {
		if(event.getNPC().hasTrait(DwarfTrainerTrait.class) && event.getNPC().getId() == getNPC().getId()) {
			plugin.getDCEntityListener().onNPCLeftClickEvent(event);
		}
	}
	
	@EventHandler
	public void onNPCRightClick(NPCRightClickEvent event) {
		if(event.getNPC().hasTrait(DwarfTrainerTrait.class) && event.getNPC().getId() == getNPC().getId()) {
			plugin.getDCEntityListener().onNPCRightClickEvent(event);
		}
	}
}
