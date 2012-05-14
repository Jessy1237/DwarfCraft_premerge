package com.Jessy1237.DwarfCraft.commands;

/**
 * Original Authors: smartaleq, LexManos and RCarretta
 */

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.Jessy1237.DwarfCraft.DwarfCraft;

public class CommandRemoveNext extends Command {
	private final DwarfCraft plugin;

	public CommandRemoveNext(final DwarfCraft plugin) {
		super("RemoveNext");
		this.plugin = plugin;
	}
	
	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args){
		if(!plugin.getDataManager().getTrainerRemove().contains((Player) sender)){
			plugin.getDataManager().getTrainerRemove().add((Player) sender);
			sender.sendMessage("Punch the trainer you want to remove.");
		}
		return true;
	}
}
