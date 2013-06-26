package com.Jessy1237.DwarfCraft;

/**
 * Original Authors: smartaleq, LexManos and RCarretta
 */

import java.util.List;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.Jessy1237.DwarfCraft.commands.CommandCreateGreeter;
import com.Jessy1237.DwarfCraft.commands.CommandCreateTrainer;
import com.Jessy1237.DwarfCraft.commands.CommandDCCommands;
import com.Jessy1237.DwarfCraft.commands.CommandDMem;
import com.Jessy1237.DwarfCraft.commands.CommandDebug;
import com.Jessy1237.DwarfCraft.commands.CommandEffectInfo;
import com.Jessy1237.DwarfCraft.commands.CommandHelp;
import com.Jessy1237.DwarfCraft.commands.CommandInfo;
import com.Jessy1237.DwarfCraft.commands.CommandListTrainers;
import com.Jessy1237.DwarfCraft.commands.CommandLookAtNext;
import com.Jessy1237.DwarfCraft.commands.CommandRace;
import com.Jessy1237.DwarfCraft.commands.CommandRaces;
import com.Jessy1237.DwarfCraft.commands.CommandRemoveNext;
import com.Jessy1237.DwarfCraft.commands.CommandRemoveTrainer;
import com.Jessy1237.DwarfCraft.commands.CommandRenameNPC;
import com.Jessy1237.DwarfCraft.commands.CommandRenameNext;
import com.Jessy1237.DwarfCraft.commands.CommandRules;
import com.Jessy1237.DwarfCraft.commands.CommandSetSkill;
import com.Jessy1237.DwarfCraft.commands.CommandSkillInfo;
import com.Jessy1237.DwarfCraft.commands.CommandSkillSheet;
import com.Jessy1237.DwarfCraft.commands.CommandTutorial;
import com.Jessy1237.DwarfCraft.events.DCBlockListener;
import com.Jessy1237.DwarfCraft.events.DCEntityListener;
import com.Jessy1237.DwarfCraft.events.DCInventoryListener;
import com.Jessy1237.DwarfCraft.events.DCPlayerListener;
import com.Jessy1237.DwarfCraft.events.DCVehicleListener;
import com.Jessy1237.DwarfCraft.events.DCWorldListener;
import com.sharesc.caliog.npclib.HumanNPC;
import com.sharesc.caliog.npclib.NPC;
import com.sharesc.caliog.npclib.NPCManager;

/**
 * 
 * DwarfCraft is a RPG-like plugin for minecraft (via Bukkit) that allows
 * players to improve their characters. Players(Dwarfs!) may pay materials to a
 * trainer to improve a skill level, which will provide benefits such as
 * increased weapon damage, decreased tool durability drop, increased drops from
 * blocks or mobs, etc.
 * 
 * Data used for this plugin comes from two places: On each load, a list of
 * skills and effects is pulled from flatfiles. Dwarf's skill levels and world
 * training zones are kept in database (currently supports only sqlite)
 * 
 * @author smartaleq
 * @author RCarretta
 * 
 */
public class DwarfCraft extends JavaPlugin {

	private final DCBlockListener blockListener = new DCBlockListener(this);
	private final DCPlayerListener playerListener = new DCPlayerListener(this);
	private final DCEntityListener entityListener = new DCEntityListener(this);
	private final DCVehicleListener vehicleListener = new DCVehicleListener(this);
	private final DCWorldListener worldListener = new DCWorldListener(this);
	private final DCInventoryListener inventoryListener = new DCInventoryListener(this);
	private ConfigManager cm;
	private DataManager dm;
	private Out out;
	private NPCManager npcm;

	public static int debugMessagesThreshold = 10;

	public ConfigManager getConfigManager() {
		return cm;
	}

	public DataManager getDataManager() {
		return dm;
	}

	public NPCManager getNPCManager() {
		return npcm;
	}

	public Out getOut() {
		return out;
	}

	public Permission perms = null;

	private boolean setupPermissions() {
		RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
		perms = rsp.getProvider();
		return perms != null;
	}

	private boolean checkPermission(CommandSender sender, String name, String type) {

		if (perms == null)
			return false;

		if (sender instanceof Player){
			if(type.equals("op")){
				return perms.has((Player) sender, ("DwarfCraft.op." + name).toLowerCase());
			}else if(type.equals("norm")){
				return perms.has((Player) sender, ("DwarfCraft.norm." + name).toLowerCase());
			} else if(type.equals("all")){
				return perms.has((Player) sender, "DwarfCraft.*".toLowerCase());
			}
		}

		return false;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		Command cmd = null;
		String name = commandLabel;
		boolean hasNorm = checkPermission(sender, name, "norm");
		boolean hasOp = checkPermission(sender, name, "op");
		boolean hasAll = checkPermission(sender, name, "all");
		boolean isCmd = true;

		if (name.equalsIgnoreCase("DCHelp")) {
			if (hasNorm || hasAll) {
				cmd = new CommandHelp(this);
			}
		} else if (name.equalsIgnoreCase("SkillSheet")) {
			if (hasNorm || hasAll) {
				cmd = new CommandSkillSheet(this);
			}
		} else if (name.equalsIgnoreCase("Tutorial")) {
			if (hasNorm || hasAll) {
				cmd = new CommandTutorial(this);
			}
		} else if (name.equalsIgnoreCase("DCInfo")) {
			if (hasNorm || hasAll) {
				cmd = new CommandInfo(this);
			}
		} else if (name.equalsIgnoreCase("DCRules")) {
			if (hasNorm || hasAll) {
				cmd = new CommandRules(this);
			}
		} else if (name.equalsIgnoreCase("DCCommands")) {
			if (hasNorm || hasAll) {
				cmd = new CommandDCCommands(this);
			}
		} else if (name.equalsIgnoreCase("SkillInfo")) {
			if (hasNorm || hasAll) {
				cmd = new CommandSkillInfo(this);
			}
		} else if (name.equalsIgnoreCase("Race")) {
			if (hasNorm || hasAll) {
				cmd = new CommandRace(this);
			}
		} else if (name.equalsIgnoreCase("EffectInfo")) {
			if (hasNorm || hasAll) {
				cmd = new CommandEffectInfo(this);
			}
		} else if (name.equalsIgnoreCase("RemoveNext")) {
			if (hasOp || hasAll) {
				cmd = new CommandRemoveNext(this);
			}
		} else if (name.equalsIgnoreCase("RenameNext")) {
			if (hasOp || hasAll) {
				cmd = new CommandRenameNext(this);
			}
		} else if (name.equalsIgnoreCase("RenameNPC")) {
			if (hasOp || hasAll) {
				cmd = new CommandRenameNPC(this);
			}
		} else if (name.equalsIgnoreCase("DCDebug")) {
			if (hasOp || hasAll) {
				cmd = new CommandDebug(this);
			}
		} else if (name.equalsIgnoreCase("ListTrainers")) {
			if (hasOp || hasAll) {
				cmd = new CommandListTrainers(this);
			}
		} else if (name.equalsIgnoreCase("RemoveTrainer")) {
			if (hasOp || hasAll) {
				cmd = new CommandRemoveTrainer(this);
			}
		} else if (name.equalsIgnoreCase("SetSkill")) {
			if (hasOp || hasAll) {
				cmd = new CommandSetSkill(this);
			}
		} else if (name.equalsIgnoreCase("CreateGreeter")) {
			if (hasOp || hasAll) {
				cmd = new CommandCreateGreeter(this);
			}
		} else if (name.equalsIgnoreCase("CreateTrainer")) {
			if (hasOp || hasAll) {
				cmd = new CommandCreateTrainer(this);
			}
		} else if (name.equalsIgnoreCase("LookAtNext")) {
			if (hasOp || hasAll) {
				cmd = new CommandLookAtNext(this);
			}
		} else if (name.equalsIgnoreCase("DMem")) {
			if (hasOp || hasAll) {
				cmd = new CommandDMem(this);
			}
		} else if (name.equalsIgnoreCase("Races")) {
			cmd = new CommandRaces(this);
		} else {
			isCmd = false;
		}

		if (cmd == null) {
			if (isCmd == false) {
				return false;
			} else {
				if (hasNorm == false && hasOp == false) {
					sender.sendMessage("§4You do not have permission to do that.");
				} else if (hasOp == false) {
					sender.sendMessage("§4You do not have permission to do that");
				}
				return true;
			}
		} else {
			return cmd.execute(sender, commandLabel, args);
		}
	}

	/**
	 * Called upon disabling the plugin.
	 */
	@Override
	public void onDisable() {
		List<NPC> npcs = npcm.getNPCs();
		for (NPC npc : npcs) {
			HumanNPC hnpc = (HumanNPC) npc;
			npcm.despawnHumanByName(hnpc.getName());
		}
	}

	/**
	 * Called upon enabling the plugin
	 */
	@Override
	public void onEnable() {
		setupPermissions();
		PluginManager pm = getServer().getPluginManager();

		pm.registerEvents(playerListener, this);

		pm.registerEvents(entityListener, this);

		pm.registerEvents(blockListener, this);

		pm.registerEvents(vehicleListener, this);

		pm.registerEvents(worldListener, this);
		
		pm.registerEvents(inventoryListener, this);

		npcm = new NPCManager(this);
		cm = new ConfigManager(this, getDataFolder().getAbsolutePath(), "DwarfCraft.config");
		dm = new DataManager(this, cm);
		out = new Out(this);

		// readGreeterMessagesfile() depends on datamanager existing, so this
		// has to go here
		if (!getConfigManager().readGreeterMessagesfile()) {
			System.out.println("[SEVERE] Failed to read DwarfCraft Greeter Messages)");
			getServer().getPluginManager().disablePlugin(this);
		}

		for (Player player : getServer().getOnlinePlayers()) {
			DCPlayer dCPlayer = getDataManager().find(player);
			if (dCPlayer == null)
				dCPlayer = getDataManager().createDwarf(player);
			if (!getDataManager().getDwarfData(dCPlayer))
				getDataManager().createDwarfData(dCPlayer);
		}

		System.out.println(getDescription().getName() + " version " + getDescription().getVersion() + " is enabled!");
	}
}
