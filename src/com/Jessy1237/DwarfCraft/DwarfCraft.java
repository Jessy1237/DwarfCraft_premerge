package com.Jessy1237.DwarfCraft;

/**
 * Original Authors: smartaleq, LexManos and RCarretta
 */

import java.util.List;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;

import com.Jessy1237.DwarfCraft.events.*;
import com.Jessy1237.DwarfCraft.commands.*;

import com.topcat.npclib.NPCManager;
import com.topcat.npclib.entity.HumanNPC;
import com.topcat.npclib.entity.NPC;

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

	private boolean checkPermission(CommandSender sender, String name, boolean opOnly) {

		if (perms == null)
			return (!opOnly || sender.isOp());

		if (sender instanceof Player){
			if(opOnly){
				return perms.has((Player) sender, ("DwarfCraft." + ("op.") + name).toLowerCase());
			}else{
				return perms.has((Player) sender, ("DwarfCraft." + ("norm.") + name).toLowerCase());
			}
		}

		return false;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		Command cmd = null;
		String name = commandLabel;
		boolean hasNorm = checkPermission(sender, name, false);
		boolean hasOp = checkPermission(sender, name, true);
		boolean isCmd = true;

		if (name.equalsIgnoreCase("DCHelp")) {
			if (hasNorm) {
				cmd = new CommandHelp(this);
			}
		} else if (name.equalsIgnoreCase("SkillSheet")) {
			if (hasNorm) {
				cmd = new CommandSkillSheet(this);
			}
		} else if (name.equalsIgnoreCase("Tutorial")) {
			if (hasNorm) {
				cmd = new CommandTutorial(this);
			}
		} else if (name.equalsIgnoreCase("Info")) {
			if (hasNorm) {
				cmd = new CommandInfo(this);
			}
		} else if (name.equalsIgnoreCase("Rules")) {
			if (hasNorm) {
				cmd = new CommandRules(this);
			}
		} else if (name.equalsIgnoreCase("DCCommands")) {
			if (hasNorm) {
				cmd = new CommandDCCommands(this);
			}
		} else if (name.equalsIgnoreCase("SkillInfo")) {
			if (hasNorm) {
				cmd = new CommandSkillInfo(this);
			}
		} else if (name.equalsIgnoreCase("Race")) {
			if (hasNorm) {
				cmd = new CommandRace(this);
			}
		} else if (name.equalsIgnoreCase("EffectInfo")) {
			if (hasNorm) {
				cmd = new CommandEffectInfo(this);
			}
		} else if (name.equalsIgnoreCase("RemoveNext")) {
			if (hasOp) {
				cmd = new CommandRemoveNext(this);
			}
		} else if (name.equalsIgnoreCase("Debug")) {
			if (hasOp) {
				cmd = new CommandDebug(this);
			}
		} else if (name.equalsIgnoreCase("ListTrainers")) {
			if (hasOp) {
				cmd = new CommandListTrainers(this);
			}
		} else if (name.equalsIgnoreCase("RemoveTrainer")) {
			if (hasOp) {
				cmd = new CommandRemoveTrainer(this);
			}
		} else if (name.equalsIgnoreCase("SetSkill")) {
			if (hasOp) {
				cmd = new CommandSetSkill(this);
			}
		} else if (name.equalsIgnoreCase("CreateGreeter")) {
			if (hasOp) {
				cmd = new CommandCreateGreeter(this);
			}
		} else if (name.equalsIgnoreCase("CreateTrainer")) {
			if (hasOp) {
				cmd = new CommandCreateTrainer(this);
			}
		} else if (name.equalsIgnoreCase("LookAtNext")) {
			if (hasOp) {
				cmd = new CommandLookAtNext(this);
			}
		} else if (name.equalsIgnoreCase("DMem")) {
			if (hasOp) {
				cmd = new CommandDMem(this);
			}
		} else {
			isCmd = false;
		}

		if (cmd == null) {
			if (isCmd == false) {
				return false;
			} else {
				if (hasNorm == false && hasOp == false) {
					sender.sendMessage("�4You do not have permission to use that.");
				} else if (hasOp == false) {
					sender.sendMessage("�4You do not have permission to use that.");
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
		// pm.registerEvent(Event.Type.WORLD_LOAD, worldListener, Priority.Low,
		// this);

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
