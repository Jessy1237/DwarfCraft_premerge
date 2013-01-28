package com.Jessy1237.DwarfCraft.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.Jessy1237.DwarfCraft.DwarfCraft;
import com.Jessy1237.DwarfCraft.Race;

/**
 * Original Authors: Jessy1237 & Curtis1509
 */

public class CommandRaces extends Command {

	private DwarfCraft plugin;

	public CommandRaces(final DwarfCraft plugin) {
		super("Races");
		this.plugin = plugin;
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		String msg = "Races:";
		for(Race r : plugin.getConfigManager().getRaceList()) {
			if (r != null) {
				msg = msg + "\n" + r.getName() + ": " + r.getDesc();
			}
		}
		sender.sendMessage(msg);
		return true;
	}

}
