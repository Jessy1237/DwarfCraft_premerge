package com.Jessy1237.DwarfCraft.commands;

/**
 * Original Authors: smartaleq, LexManos and RCarretta
 */

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.citizensnpcs.api.npc.AbstractNPC;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.Jessy1237.DwarfCraft.CommandInformation;
import com.Jessy1237.DwarfCraft.CommandParser;
import com.Jessy1237.DwarfCraft.DCCommandException;
import com.Jessy1237.DwarfCraft.DwarfCraft;
import com.Jessy1237.DwarfCraft.DwarfTrainerTrait;

public class CommandCreateGreeter extends Command {
	private final DwarfCraft plugin;

	public CommandCreateGreeter(final DwarfCraft plugin) {
		super("CreateGreeter");
		this.plugin = plugin;
	}

	@SuppressWarnings("deprecation")
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
				String type = "Type";
				desiredArguments.add(uniqueId);
				desiredArguments.add(name);
				desiredArguments.add(greeterMessage);
				desiredArguments.add(type);
				outputList = parser.parse(desiredArguments, false);
				uniqueId = (String) outputList.get(0);
				name = (String) outputList.get(1);
				greeterMessage = (String) outputList.get(2);
				type = (String) outputList.get(3);
				
				Location location = ((Player) sender).getLocation();
				if(plugin.getNPCRegistry().getById(Integer.parseInt(uniqueId)) != null ){
					plugin.getOut().sendMessage(sender, "An NPC with that ID already exsists! Try another ID.");
					return false;
				}
				AbstractNPC npc;
				if(type.equalsIgnoreCase("PLAYER")) {
					npc = (AbstractNPC) plugin.getNPCRegistry().createNPC(EntityType.PLAYER, UUID.randomUUID(), Integer.parseInt(uniqueId), name);
				} else {
					npc = (AbstractNPC) plugin.getNPCRegistry().createNPC(EntityType.fromName(type), UUID.randomUUID(), Integer.parseInt(uniqueId), name);
				}
				npc.spawn(location);
				npc.addTrait(new DwarfTrainerTrait(plugin, Integer.parseInt(uniqueId),null, null, null, true, greeterMessage));
				npc.setProtected(true);
			} catch (DCCommandException e) {
				e.describe(sender);
				sender.sendMessage(CommandInformation.Usage.CREATEGREETER.getUsage());
				return false;
			}
		}
		return true;
	}
}
