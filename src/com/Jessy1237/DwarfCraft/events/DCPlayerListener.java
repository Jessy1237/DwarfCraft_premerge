package com.Jessy1237.DwarfCraft.events;

/**
 * Original Authors: smartaleq, LexManos and RCarretta
 */

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import com.Jessy1237.DwarfCraft.DCCraftSchedule;
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
		if (!dm.getDwarfData(data))
			dm.createDwarfData(data);
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
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerInteract(PlayerInteractEvent event) {

		if (plugin.getConfigManager().worldBlacklist) {
			for (World w : plugin.getConfigManager().worlds) {
				if (w != null) {
					if (event.getPlayer().getWorld() == w) {
						return;
					}
				}
			}
		}

		// Crafting changes

		int origHealth = event.getPlayer().getFoodLevel();
		int origItemAmount = event.getPlayer().getItemInHand().getAmount();

		if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getType() == Material.WORKBENCH) {
			DCCraftSchedule sched = new DCCraftSchedule(plugin, plugin.getDataManager().find(event.getPlayer()));
			int id = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, sched, 0, 2);
			sched.setID(id);
		}

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

		// EffectType.EAT
		if ((event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) && item.getType() != Material.FISHING_ROD) {
			for (Skill s : skills.values()) {
				for (Effect e : s.getEffects()) {
					if (e.getEffectType() == EffectType.EAT && (e.checkInitiator(item))) {
						int health = Util.randomAmount((e.getEffectAmount(dcPlayer)));

						if (DwarfCraft.debugMessagesThreshold < 8)
							System.out.println(String.format("DC8: Are Food: \"%s\" for %d health", Util.getCleanName(item), health));

						Block block = event.getClickedBlock();

						if (event.isCancelled()) {
							return;
						}

						onPlayerEat(event, item, health, origHealth, origItemAmount, block);

					} else if (event.getClickedBlock() != null) {
						if (e.getEffectType() == EffectType.EAT && e.checkInitiator(event.getClickedBlock().getTypeId(), event.getClickedBlock().getData())) {
							int health = Util.randomAmount((e.getEffectAmount(dcPlayer)));

							if (DwarfCraft.debugMessagesThreshold < 8)
								System.out.println(String.format("DC8: Are Food: \"%s\" for %d health", event.getClickedBlock().getType().toString(), health));

							Block block = event.getClickedBlock();

							if (event.isCancelled()) {
								return;
							}
							onPlayerEat(event, item, health, origHealth, origItemAmount, block);
						}
					}
				}
			}
		}
	}

	public void onPlayerEat(PlayerInteractEvent event, ItemStack item, int health, int origHealth, int origItemAmount, Block block) {

		int id = item.getTypeId();
		int bId = 0;
		if (block != null)
			bId = block.getTypeId();
		int itemFoodLevel = 0;
		float itemSaturationLevel = 0.0f;

		// Raw Fish
		if (id == 349) {
			itemFoodLevel = 2;
			itemSaturationLevel = 1.2f;

			// Mushroom Soup
		} else if (id == 282) {
			itemFoodLevel = 8;
			itemSaturationLevel = 9.6f;

			// Raw Pork
		} else if (id == 319) {
			itemFoodLevel = 3;
			itemSaturationLevel = 1.8f;

			// Cooked Pork
		} else if (id == 320) {
			itemFoodLevel = 9;
			itemSaturationLevel = 12.8f;

			// Cake
		} else if (bId == 92) {
			itemFoodLevel = 2;
			itemSaturationLevel = 0.4f;
		}

		if (event.isCancelled()) {
			return;
		}
		if(((origHealth - itemFoodLevel) + health) > 20){
			if(bId == 92){
				return;
			}
			event.getPlayer().setFoodLevel(20);
			event.getPlayer().setSaturation(event.getPlayer().getSaturation() + itemSaturationLevel);
		}else{
			event.getPlayer().setFoodLevel((origHealth - itemFoodLevel) + health);
			event.getPlayer().setSaturation(event.getPlayer().getSaturation() + itemSaturationLevel);	
		}
	}

	/**
	 * Called when a player opens an inventory
	 * 
	 * @param event
	 *            Relevant event details
	 */
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onInventoryOpen(InventoryOpenEvent event) {

		if (plugin.getConfigManager().worldBlacklist) {
			for (World w : plugin.getConfigManager().worlds) {
				if (w != null) {
					if (event.getPlayer().getWorld() == w) {
						return;
					}
				}
			}
		}

		DCCraftSchedule sched = new DCCraftSchedule(plugin, plugin.getDataManager().find((Player) event.getPlayer()));
		int id = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, sched, 0, 2);
		sched.setID(id);
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerFish(PlayerFishEvent event) {

		if (plugin.getConfigManager().worldBlacklist) {
			for (World w : plugin.getConfigManager().worlds) {
				if (w != null) {
					if (event.getPlayer().getWorld() == w) {
						return;
					}
				}
			}
		}

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