package com.Jessy1237.DwarfCraft.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.Jessy1237.DwarfCraft.CommandInformation;
import com.Jessy1237.DwarfCraft.DwarfCraft;

public class CommandTypeNext extends Command {
	private final DwarfCraft plugin;

	public CommandTypeNext(final DwarfCraft plugin) {
		super("TypeNext");
		this.plugin = plugin;
	}
	
	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		if (args.length == 0) {
			plugin.getOut().sendMessage(sender, CommandInformation.Usage.TYPENEXT.getUsage());
		} else if (args[0].equalsIgnoreCase("?")) {
			plugin.getOut().sendMessage(sender, CommandInformation.Desc.TYPENEXT.getDesc());
		} else {

			if (!plugin.getDataManager().getType().containsKey((Player) sender)) {
				plugin.getDataManager().getType().put((Player) sender, args[0]);
				sender.sendMessage("Punch the trainer/greeter you want to change.");
			}
		}
		return true;
	}
}
