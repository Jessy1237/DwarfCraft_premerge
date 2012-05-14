package com.Jessy1237.DwarfCraft.commands;

/**
 * Original Authors: Jessy1237 & Curtis1509
 */

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.Jessy1237.DwarfCraft.DwarfCraft;

public class CommandLookAtNext extends Command {
	private final DwarfCraft plugin;

	public CommandLookAtNext(final DwarfCraft plugin) {
		super("LookAtNext");
		this.plugin = plugin;
	}
	
	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args){
		if(!plugin.getDataManager().getTrainerLookAt().contains((Player) sender)){
			plugin.getDataManager().getTrainerLookAt().add((Player) sender);
			sender.sendMessage("Punch the trainer you want to look at you.");
		}
		return true;
	}
}
