package com.Jessy1237.DwarfCraft.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.Jessy1237.DwarfCraft.CommandInformation;
import com.Jessy1237.DwarfCraft.DwarfCraft;

public class CommandChangeType extends Command {
	private final DwarfCraft plugin;

	public CommandChangeType(final DwarfCraft plugin) {
		super("TypeChange");
		this.plugin = plugin;
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		if (args.length < 2) {
			plugin.getOut().sendMessage(sender, CommandInformation.Usage.TYPECHANGE.getUsage());
		} else if (args[0].equalsIgnoreCase("?")) {
			plugin.getOut().sendMessage(sender, CommandInformation.Desc.TYPECHANGE.getDesc());
		} else {
			plugin.getDataManager().getTrainerByName(args[0]).setType(args[1]);
		}
		return true;
	}
}
