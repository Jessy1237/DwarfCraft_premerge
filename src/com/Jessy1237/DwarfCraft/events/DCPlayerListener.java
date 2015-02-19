package com.Jessy1237.DwarfCraft.events;

/**
 * Original Authors: smartaleq, LexManos and RCarretta
 */

import java.util.HashMap;

import net.minecraft.server.v1_8_R1.EntityPlayer;
import net.minecraft.server.v1_8_R1.EnumPlayerInfoAction;
import net.minecraft.server.v1_8_R1.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_8_R1.PacketPlayOutPlayerInfo;

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
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import com.Jessy1237.DwarfCraft.DCPlayer;
import com.Jessy1237.DwarfCraft.DataManager;
import com.Jessy1237.DwarfCraft.DwarfCraft;
import com.Jessy1237.DwarfCraft.Effect;
import com.Jessy1237.DwarfCraft.EffectType;
import com.Jessy1237.DwarfCraft.Skill;
import com.Jessy1237.DwarfCraft.Util;
import com.sharesc.caliog.npclib.NPC;
import com.sharesc.caliog.npclib.NPCUtils;

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
		for(NPC npc : plugin.getNPCManager().getNPCs()) {
			NPCUtils.sendPacketNearby(npc.getBukkitEntity().getLocation(), new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, (EntityPlayer)npc.getEntity()));
			NPCUtils.sendPacketNearby(npc.getBukkitEntity().getLocation(), new PacketPlayOutNamedEntitySpawn((EntityPlayer)npc.getEntity()));
		}
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
	@SuppressWarnings("deprecation")
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
							if(((origFoodLevel - 2) + foodLevel) > 20){
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
		Player player = event.getPlayer();
		ItemStack item = event.getItem();
		int id = item.getTypeId();
		DCPlayer dcPlayer = plugin.getDataManager().find(player);
		HashMap<Integer, Skill> skills = dcPlayer.getSkills();
		int lvl = FoodLevel.getLvl(id);
		
		if(lvl == 0) {
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
	}

	@SuppressWarnings("deprecation")
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
	
	public enum FoodLevel {

		APPLE(260,4),
		BAKED_POTATO(393,6),
		BREAD(297,5),
		CAKE(92,2),
		CARROT(391,4),
		COOKED_CHICKEN(366,6),
		COOKED_FISH(350,5),
		COOKED_PORKCHOP(320,8),
		COOKIE(357,2),
		GOLDEN_APPLE(322,4),
		GOLDEN_CARROT(396,6),
		MELON(360,2),
		MUSHROOM_STEW(282,6),
		POISONOUS_POTATO(394,2),
		POTATO(392,1),
		PUMPKIN_PIE(400,8),
		RAW_BEEF(363,3),
		RAW_CHICKEN(365,2),
		RAW_FISH(349,2),
		RAW_PORKCHOP(319,3),
		ROTTEN_FLESH(367,4),
		SPIDER_EYE(375,2),
		STEAK(364,8);
		
		private int lvl;
		private int id;

		private FoodLevel(int id, int lvl) {
			this.lvl = lvl;
		}

		public int getId() {
			return id;
		}
		
		public int getLevel() {
			return this.lvl;
		}
		
		public static int getLvl(int id) {
			for(FoodLevel f : FoodLevel.values()) {
				if(f != null) {
					if(f.getId() == id) {
						return f.getLevel();
					}
				}
			}
			return 0;
		}
		
	}
}