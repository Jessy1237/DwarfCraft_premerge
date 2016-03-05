package com.Jessy1237.DwarfCraft.commands;

/**
 * Original Authors: smartaleq, LexManos and RCarretta
 */

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.Jessy1237.DwarfCraft.DwarfCraft;

public class CommandHelp extends Command
{
	private final DwarfCraft	plugin;
	
	public CommandHelp(final DwarfCraft plugin)
	{
		super("DCHelp");
		this.plugin = plugin;
	}
	
	@Override
	public boolean execute(CommandSender sender, String commandLabel,
			String[] args)
	{
		if (DwarfCraft.debugMessagesThreshold < 1)
			System.out.println("DC1: started command 'dchelp'");
		plugin.getOut().help(sender);
		return true;
	}
}
