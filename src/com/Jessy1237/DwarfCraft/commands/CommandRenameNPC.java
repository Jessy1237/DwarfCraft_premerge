package com.Jessy1237.DwarfCraft.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.Jessy1237.DwarfCraft.CommandInformation;
import com.Jessy1237.DwarfCraft.DwarfCraft;

public class CommandRenameNPC extends Command {
	private final DwarfCraft plugin;

	public CommandRenameNPC(final DwarfCraft plugin) {
		super("RenameNPC");
		this.plugin = plugin;
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		if (args.length < 2) {
			plugin.getOut().sendMessage(sender, CommandInformation.Usage.RENAMENPC.getUsage());
		} else if (args[0].equalsIgnoreCase("?")) {
			plugin.getOut().sendMessage(sender, CommandInformation.Desc.RENAMENPC.getDesc());
		} else {
			plugin.getDataManager().getTrainerById(args[0]).setDisplayName(args[1]);
		}
		return true;
	}
}