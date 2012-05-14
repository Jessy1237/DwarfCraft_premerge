package com.Jessy1237.DwarfCraft.commands;

/**
 * Original Authors: smartaleq, LexManos and RCarretta
 */

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.Jessy1237.DwarfCraft.CommandInformation;
import com.Jessy1237.DwarfCraft.CommandParser;
import com.Jessy1237.DwarfCraft.DCCommandException;
import com.Jessy1237.DwarfCraft.DwarfCraft;

public class CommandRemoveTrainer extends Command {
	private final DwarfCraft plugin;

	public CommandRemoveTrainer(final DwarfCraft plugin) {
		super("RemoveTrainer");
		this.plugin = plugin;
	}
	
	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args){
		if (DwarfCraft.debugMessagesThreshold < 1)
			System.out.println("DC1: started command 'removetrainer'");

		if (args.length==0) {
			plugin.getOut().sendMessage(sender, CommandInformation.Usage.REMOVETRAINER.getUsage());
		} else if (args[0].equalsIgnoreCase("?")) {
			plugin.getOut().sendMessage(sender, CommandInformation.Desc.REMOVETRAINER.getDesc());
		}else{
			try{
				CommandParser parser = new CommandParser(plugin, sender, args);
				List<Object> desiredArguments = new ArrayList<Object>();
				List<Object> outputList = null;
				
				desiredArguments.add("UniqueIDRmv");
				outputList = parser.parse(desiredArguments, false);
				plugin.getDataManager().removeTrainer((String)outputList.get(0));
				
			} catch (DCCommandException e) {
				e.describe(sender);
				sender.sendMessage(CommandInformation.Usage.REMOVETRAINER.getUsage());
				return false;		
			}
		}
		return true;
		
	}
}
