package com.Jessy1237.DwarfCraft.events;

/**
 * Original Authors: smartaleq, LexManos and RCarretta
 */

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.ItemStack;

import com.Jessy1237.DwarfCraft.DCPlayer;
import com.Jessy1237.DwarfCraft.DataManager;
import com.Jessy1237.DwarfCraft.DwarfCraft;
import com.Jessy1237.DwarfCraft.Effect;
import com.Jessy1237.DwarfCraft.EffectType;
import com.Jessy1237.DwarfCraft.Skill;
import com.Jessy1237.DwarfCraft.Util;

public class DCPlayerListener implements Listener {
	private final DwarfCraft plugin;

	public DCPlayerListener(final DwarfCraft plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerQuit(PlayerQuitEvent event) {
	}

	/**
	 * When a player joins the server this initialized their data from the
	 * database or creates new info for them.
	 * 
	 * also broadcasts a welcome "player" message
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerJoin(PlayerJoinEvent event) {
		DataManager dm = plugin.getDataManager();
		Player player = event.getPlayer();
		DCPlayer data = dm.find(player);

		if (data == null)
			data = dm.createDwarf(player);
		plugin.getDataManager().checkDwarfData(data);
		if (!plugin.getConfigManager().sendGreeting)
			return;

		plugin.getOut().welcome(plugin.getServer(), data);
	}

	/**
	 * Called when a player interacts
	 * 
	 * @param event
	 *            Relevant event details
	 */
	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (!Util.isWorldAllowed(event.getPlayer().getWorld()))
			return;

		Player player = event.getPlayer();
		DCPlayer dcPlayer = plugin.getDataManager().find(player);
		HashMap<Integer, Skill> skills = dcPlayer.getSkills();

		ItemStack item = player.getItemInHand();

		// EffectType.PLOWDURABILITY
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Block block = event.getClickedBlock();
			Material material = block.getType();

			if (material == Material.DIRT || material == Material.GRASS) {
				for (Skill s : skills.values()) {
					for (Effect effect : s.getEffects()) {
						if (effect.getEffectType() == EffectType.PLOWDURABILITY && effect.checkTool(item)) {
							effect.damageTool(dcPlayer, 1, item);
							// block.setTypeId(60);
						}
					}
				}
			}
		}

		Block block = event.getClickedBlock();
		int origFoodLevel = event.getPlayer().getFoodLevel();

		// EffectType.EAT
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			for (Skill s : skills.values()) {
				for (Effect e : s.getEffects()) {
					if (e.getEffectType() == EffectType.EAT && e.checkInitiator(block.getTypeId(), block.getData())) {

						int foodLevel = Util.randomAmount((e.getEffectAmount(dcPlayer)));

						if (block.getTypeId() == 92) {
							if (((origFoodLevel - 2) + foodLevel) > 20) {
								event.getPlayer().setFoodLevel(20);
								event.getPlayer().setSaturation(event.getPlayer().getSaturation() + 0.4f);
							} else {
								event.getPlayer().setFoodLevel((origFoodLevel - 2) + foodLevel);
								event.getPlayer().setSaturation(event.getPlayer().getSaturation() + 0.4f);
							}
						}
					}
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
		if (!Util.isWorldAllowed(event.getPlayer().getWorld()))
			return;

		Player player = event.getPlayer();
		ItemStack item = event.getItem();
		int id = item.getTypeId();
		DCPlayer dcPlayer = plugin.getDataManager().find(player);
		HashMap<Integer, Skill> skills = dcPlayer.getSkills();
		int lvl = Util.FoodLevel.getLvl(id);

		if (lvl == 0) {
			return;
		}

		for (Skill s : skills.values()) {
			for (Effect e : s.getEffects()) {
				if (e.getEffectType() == EffectType.EAT && e.checkInitiator(item)) {
					int foodLevel = Util.randomAmount((e.getEffectAmount(dcPlayer)));
					player.setFoodLevel((player.getFoodLevel() - lvl) + foodLevel);
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerShearEntityEvent(PlayerShearEntityEvent event) {		
		if (!Util.isWorldAllowed(event.getPlayer().getWorld()))
			return;

		Player player = event.getPlayer();
		Entity entity = event.getEntity();
		DCPlayer dcPlayer = plugin.getDataManager().find(player);
		HashMap<Integer, Skill> skills = dcPlayer.getSkills();
		boolean changed = false;

		for (Skill s : skills.values()) {
			for (Effect e : s.getEffects()) {
				if (e.getEffectType() == EffectType.SHEAR) {
					if (entity.getType() == EntityType.SHEEP && e.checkMob(entity)) {
						Sheep sheep = (Sheep) entity;
						if (!sheep.isSheared()) {
							if (sheep.isAdult()) {
								ItemStack item = e.getOutput(dcPlayer, sheep.getColor().getWoolData(), -1);
								entity.getWorld().dropItemNaturally(entity.getLocation(), item);
								sheep.setSheared(true);
								changed = true;
							}
						}
					} else if (entity.getType() == EntityType.MUSHROOM_COW && e.checkMob(entity)) {
						MushroomCow mooshroom = (MushroomCow) entity;
						if (mooshroom.isAdult()) {
							
							Entity newE = entity.getWorld().spawnEntity(entity.getLocation(), EntityType.COW);
							Cow cow = (Cow) newE;
							cow.setAge(mooshroom.getAge());
							cow.setAdult();
							cow.setBreed(mooshroom.canBreed());
							cow.setAgeLock(mooshroom.getAgeLock());
							cow.setHealth(mooshroom.getHealth());
							cow.setCustomName(mooshroom.getCustomName());
							cow.setCustomNameVisible(mooshroom.isCustomNameVisible());
							cow.setTicksLived(mooshroom.getTicksLived());
							cow.setTarget(mooshroom.getTarget());
							
							ItemStack item = e.getOutput(dcPlayer);
							entity.getWorld().dropItemNaturally(entity.getLocation(), item);
							changed = true;
							
							entity.remove();
						}
					}
				}
			}
		}

		if (changed)
			event.setCancelled(true);
	}

	/**
	 * Called when a player opens an inventory
	 * 
	 * @param event
	 *            Relevant event details
	 */

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerFish(PlayerFishEvent event) {
		if (!Util.isWorldAllowed(event.getPlayer().getWorld()))
			return;
		
		if (event.isCancelled())
			return;

		if (event.getState() == State.CAUGHT_FISH) {
			DCPlayer player = plugin.getDataManager().find(event.getPlayer());
			ItemStack item = ((Item) event.getCaught()).getItemStack();
			byte meta = item.getData().getData();
			Location loc = player.getPlayer().getLocation();
			ItemStack tool = player.getPlayer().getItemInHand();
			if (item.getType() == Material.RAW_FISH) {
				for (Skill skill : player.getSkills().values()) {
					for (Effect effect : skill.getEffects()) {
						if (effect.getEffectType() == EffectType.FISH) {
							ItemStack drop = effect.getOutput(player, meta);
							if (drop.getAmount() > 0)
								loc.getWorld().dropItemNaturally(loc, drop);
						}
					}
				}

				if (tool != null && tool.getType().getMaxDurability() > 0) {
					for (Skill s : player.getSkills().values()) {
						for (Effect e : s.getEffects()) {
							if (e.getEffectType() == EffectType.RODDURABILITY && e.checkTool(tool))
								e.damageTool(player, 1, tool);
						}
					}
				}
			}
		}

	}
}