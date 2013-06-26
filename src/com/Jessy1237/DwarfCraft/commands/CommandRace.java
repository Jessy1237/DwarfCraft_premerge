package com.Jessy1237.DwarfCraft.commands;

/**
 * Original Authors: smartaleq, LexManos and RCarretta
 */

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.Jessy1237.DwarfCraft.CommandInformation;
import com.Jessy1237.DwarfCraft.DCPlayer;
import com.Jessy1237.DwarfCraft.DwarfCraft;

public class CommandRace extends Command {
	private final DwarfCraft plugin;

	public CommandRace(final DwarfCraft plugin) {
		super("Race");
		this.plugin = plugin;
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		if (DwarfCraft.debugMessagesThreshold < 1)
			System.out.println("DC1: started command 'race'");

		if (args.length == 0 && sender instanceof Player) {
			plugin.getOut().race(sender, (Player) sender);
		} else if (args.length < 2) {
			plugin.getOut().sendMessage(sender, CommandInformation.Usage.RACE.getUsage());
		} else if (args[0].equalsIgnoreCase("?")) {
			plugin.getOut().sendMessage(sender, CommandInformation.Desc.RACE.getDesc());
		} else {
			String newRace = args[0];
			DCPlayer dCPlayer = plugin.getDataManager().find((Player) sender);
			boolean confirmed = false;
			if (args[1] != null) {
				if (args[1].equalsIgnoreCase("confirm")) {
					confirmed = true;
				}
			}
			race(newRace, confirmed, dCPlayer, sender);
		}
		return true;
	}

	private void race(String newRace, boolean confirm, DCPlayer dCPlayer, CommandSender sender) {
		if (dCPlayer.getRace() == newRace) {
			plugin.getOut().alreadyRace(sender, dCPlayer, newRace);
		} else {
			if (confirm) {
				if (plugin.getConfigManager().getRace(newRace) != null) {
					plugin.getOut().changedRace(sender, dCPlayer, newRace);
					dCPlayer.changeRace(newRace);
				} else {
					plugin.getOut().dExistRace(sender, dCPlayer, newRace);
				}
			} else {
				plugin.getOut().confirmRace(sender, dCPlayer, newRace);
			}
		}
	}
}
