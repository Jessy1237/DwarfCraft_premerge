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

		if (args.length == 0) {
			plugin.getOut().race(sender, (Player) sender);
		} else if (args.length < 2) {
			plugin.getOut().sendMessage(sender, CommandInformation.Usage.RACE.getUsage());
		} else if (args[0].equalsIgnoreCase("?")) {
			plugin.getOut().sendMessage(sender, CommandInformation.Desc.RACE.getDesc());
		} else if (args.length == 3) {
			String newRace = args[1];
			String name = args[0];
			DCPlayer dCPlayer = plugin.getDataManager().find(plugin.getServer().getPlayer(name));
			boolean confirmed = false;
			if (args[2] != null) {
				if (args[2].equalsIgnoreCase("confirm")) {
					confirmed = true;
				}
			}
			if(sender instanceof Player) {
				if(plugin.perms.has(sender, "dwarfcraft.op.race")) {
					race(newRace, confirmed, dCPlayer, (CommandSender) plugin.getServer().getPlayer(name));
				}
			} else {
				race(newRace, confirmed, dCPlayer, sender);
			}
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
					if (sender instanceof Player) {
						if (plugin.perms.has((Player) sender, "dwarfcraft.norm.race." + newRace.toLowerCase())) {
							if (sender instanceof Player)
								plugin.getOut().changedRace(sender, dCPlayer, plugin.getConfigManager().getRace(newRace).getName());
							dCPlayer.changeRace(newRace);
						} else {
							sender.sendMessage("§4You do not have permission to do that.");
						}
					} else {
						plugin.getOut().changedRace(dCPlayer.getPlayer(), dCPlayer, plugin.getConfigManager().getRace(newRace).getName());
						dCPlayer.changeRace(newRace);
					}
				} else {
					if (sender instanceof Player)
						plugin.getOut().dExistRace(sender, dCPlayer, newRace);
				}
			} else {
				if (sender instanceof Player)
					plugin.getOut().confirmRace(sender, dCPlayer, newRace);
			}
		}
	}
}
