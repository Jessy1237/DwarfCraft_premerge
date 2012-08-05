package com.Jessy1237.DwarfCraft.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.Jessy1237.DwarfCraft.CommandInformation;
import com.Jessy1237.DwarfCraft.DwarfCraft;

public class CommandRenameNext extends Command {
	private final DwarfCraft plugin;

	public CommandRenameNext(final DwarfCraft plugin) {
		super("RenameNext");
		this.plugin = plugin;
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		if (args.length == 0) {
			plugin.getOut().sendMessage(sender, CommandInformation.Usage.RENAMENEXT.getUsage());
		} else if (args[0].equalsIgnoreCase("?")) {
			plugin.getOut().sendMessage(sender, CommandInformation.Desc.RENAMENEXT.getDesc());
		} else {

			if (!plugin.getDataManager().getRename().containsKey((Player) sender)) {
				plugin.getDataManager().getRename().put((Player) sender, args[0]);
				sender.sendMessage("Punch the trainer/greeter you want to rename");
			}
		}
		return true;
	}
}
