package com.Jessy1237.DwarfCraft.commands;

/**
 * Original Authors: smartaleq, LexManos and RCarretta
 */

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.Jessy1237.DwarfCraft.CommandInformation;
import com.Jessy1237.DwarfCraft.CommandParser;
import com.Jessy1237.DwarfCraft.DCCommandException;
import com.Jessy1237.DwarfCraft.DwarfCraft;
import com.Jessy1237.DwarfCraft.DwarfTrainer;

public class CommandCreateGreeter extends Command {
	private final DwarfCraft plugin;

	public CommandCreateGreeter(final DwarfCraft plugin) {
		super("CreateGreeter");
		this.plugin = plugin;
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		if (DwarfCraft.debugMessagesThreshold < 1)
			System.out.println("DC1: started command 'creategreeter'");

		if (args.length == 0) {
			plugin.getOut().sendMessage(sender, CommandInformation.Usage.CREATEGREETER.getUsage());
		} else if (args[0].equalsIgnoreCase("?")) {
			plugin.getOut().sendMessage(sender, CommandInformation.Desc.CREATEGREETER.getDesc());
		} else {
			try {
				CommandParser parser = new CommandParser(plugin, sender, args);
				List<Object> desiredArguments = new ArrayList<Object>();
				List<Object> outputList = null;

				String uniqueId = "UniqueIdAdd";
				String name = "Name";
				String greeterMessage = "GreeterMessage";
				desiredArguments.add(uniqueId);
				desiredArguments.add(name);
				desiredArguments.add(greeterMessage);
				outputList = parser.parse(desiredArguments, false);
				uniqueId = (String) outputList.get(0);
				name = (String) outputList.get(1);
				greeterMessage = (String) outputList.get(2);
				
				Location location = ((Player) sender).getLocation();
				if(plugin.getNPCRegistry().getById(Integer.parseInt(uniqueId)) != null ){
					plugin.getOut().sendMessage(sender, "An NPC with that ID already exsists! Try another ID.");
					return false;
				}
				DwarfTrainer d = new DwarfTrainer(plugin, location, Integer.parseInt(uniqueId), name, null, null, null, greeterMessage, true, false, 0);
				plugin.getDataManager().insertTrainer(d);
			} catch (DCCommandException e) {
				e.describe(sender);
				sender.sendMessage(CommandInformation.Usage.CREATEGREETER.getUsage());
				return false;
			}
		}
		return true;
	}
}
