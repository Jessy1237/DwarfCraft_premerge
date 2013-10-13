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
import com.Jessy1237.DwarfCraft.Skill;
import com.Jessy1237.DwarfCraft.DCCommandException.Type;

public class CommandCreateTrainer extends Command {
	private final DwarfCraft plugin;

	public CommandCreateTrainer(final DwarfCraft plugin) {
		super("CreateTrainer");
		this.plugin = plugin;
	}
	
	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args){
		if (DwarfCraft.debugMessagesThreshold < 1)
			System.out.println("DC1: started command 'createtrainer'");

		if (args.length==0||args[0].equals(null)) {
			plugin.getOut().sendMessage(sender, CommandInformation.Usage.CREATETRAINER.getUsage());
		} else if (args[0].equalsIgnoreCase("?")) {
			plugin.getOut().sendMessage(sender, CommandInformation.Desc.CREATETRAINER.getDesc());
		}else{
			try{				
				CommandParser parser = new CommandParser(plugin, sender, args);
				List<Object> desiredArguments = new ArrayList<Object>();
				List<Object> outputList = null;

				String uniqueId = "UniqueIdAdd";
				String name = "Name";
				Skill skill = new Skill(0, null, 0, null, null, null, null, null);
				Integer maxSkill = 1;
				Integer minSkill = 1;
				desiredArguments.add(uniqueId);
				desiredArguments.add(name);
				desiredArguments.add(skill);
				desiredArguments.add(maxSkill);
				desiredArguments.add(minSkill);
				try {
					if (!(sender instanceof Player))
							throw new DCCommandException(plugin, Type.CONSOLECANNOTUSE);
					outputList = parser.parse(desiredArguments, false);
					uniqueId   = (String)outputList.get(0);
					name       = (String)outputList.get(1);
					skill      = (Skill)outputList.get(2);
					maxSkill   = (Integer)outputList.get(3);
					minSkill   = (Integer)outputList.get(4);
				} catch (DCCommandException e) {
					if (e.getType() == Type.TOOFEWARGS) {
						outputList = parser.parse(desiredArguments, true);
						uniqueId   = (String)outputList.get(0);
						name       = (String)outputList.get(1);
						skill      = (Skill)outputList.get(2);
						maxSkill   = (Integer)outputList.get(3);
						minSkill   = (Integer)outputList.get(4);
					} else
						throw e;
				}
				
				if(minSkill == 0) {
					minSkill = -1;
				}
				
				Player p = (Player)sender;
				Location location = new Location(p.getWorld(),p.getLocation().getX() , p.getLocation().getY(), p.getLocation().getZ(), p.getLocation().getYaw() - 180, p.getLocation().getPitch());
				DwarfTrainer d = new DwarfTrainer(plugin, location,
						uniqueId, name, skill.getId(), maxSkill, minSkill, null, false, false, 0);
				plugin.getDataManager().insertTrainer(d);
			} catch (DCCommandException e) {
				e.describe(sender);
				sender.sendMessage(CommandInformation.Usage.CREATETRAINER.getUsage());
				return false;		
			}
		}
		return true;		
	}
}
