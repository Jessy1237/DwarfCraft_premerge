package com.Jessy1237.DwarfCraft.events;

/**
 * Original Authors: smartaleq, LexManos and RCarretta
 */

import java.util.HashMap;
import java.util.Random;

import org.bukkit.CropState;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Crops;
import org.bukkit.material.MaterialData;

import com.Jessy1237.DwarfCraft.DCPlayer;
import com.Jessy1237.DwarfCraft.DwarfCraft;
import com.Jessy1237.DwarfCraft.Effect;
import com.Jessy1237.DwarfCraft.EffectType;
import com.Jessy1237.DwarfCraft.Skill;
import com.Jessy1237.DwarfCraft.Util;

public class DCBlockListener implements Listener {
	private final DwarfCraft plugin;

	public DCBlockListener(final DwarfCraft plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockFromTo(BlockFromToEvent event) {

		// Code to prevent water from normally breaking crops
		// Added due to players being able to bypass DC skill restrictions
		if (event.getToBlock().getType() == Material.CROPS) {
			// Might have to add checks for carrots, reeds, potatoes and cacti
			// at some point...

			Block toBlock = event.getToBlock(); // Get the crop block
			Crops crops = (Crops) toBlock.getState().getData(); // Get the crop
																// block
																// material data

			// Cheap way to break block without causing drops
			toBlock.setType(Material.AIR);

			ItemStack itemDrops;

			if (crops.getState() == CropState.RIPE) { // If the wheat is fully
														// grown, drop one wheat
				itemDrops = new ItemStack(Material.WHEAT);
				itemDrops.setAmount(1); // I'm keeping the drop amount here in
										// case I wish to alter it at a later
										// stage, setting?
			} else { // If the wheat is no fully grown, drop a seed
				itemDrops = new ItemStack(Material.SEEDS);
				itemDrops.setAmount(1);
			}

			toBlock.getLocation().getWorld().dropItemNaturally(toBlock.getLocation(), itemDrops);
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockBreak(BlockBreakEvent event) {

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

		if (event.getPlayer().getGameMode() == GameMode.CREATIVE)
			return;

		DCPlayer player = plugin.getDataManager().find(event.getPlayer());
		HashMap<Integer, Skill> skills = player.getSkills();

		ItemStack tool = player.getPlayer().getItemInHand();
		Block block = event.getBlock();
		Location loc = event.getBlock().getLocation();
		int blockID = event.getBlock().getTypeId();
		byte meta = event.getBlock().getData();

		boolean blockDropChange = false;
		for (Skill s : skills.values()) {
			for (Effect effect : s.getEffects()) {
				if (effect.getEffectType() == EffectType.BLOCKDROP && effect.checkInitiator(blockID, meta)) {

					// Crops special line:
					if (effect.getInitiatorId() == 59) {
						if (meta != 7)
							return;
					}

					if (effect.checkTool(tool)) {
						ItemStack item = effect.getOutput(player, meta);
						ItemStack item1 = null;

						if (item.getTypeId() != 351) {
							item.setDurability(block.getData());
						}

						// Makes sure that the right blocks are dropped
						// according to metadata for sand and wood planks
						if (block.getTypeId() == 12 || block.getTypeId() == 5) {
							item.setData(new MaterialData(item.getTypeId(), meta));
						}

						// Makes sure the correct log is dropped
						if (event.getBlock().getTypeId() == 17) {
							final ItemStack old = item;
							item = new ItemStack(Material.LOG, old.getAmount());
							if (block.getData() == 0 || block.getData() == 4 || block.getData() == 8 || block.getData() == 12) {
								item.setDurability((short) 0);
							}
							if (block.getData() == 1 || block.getData() == 5 || block.getData() == 9 || block.getData() == 13) {
								item.setDurability((short) 1);
							}
							if (block.getData() == 2 || block.getData() == 6 || block.getData() == 10 || block.getData() == 14) {
								item.setDurability((short) 2);
							}
							if (block.getData() == 3 || block.getData() == 7 || block.getData() == 11 || block.getData() == 15) {
								item.setDurability((short) 3);
							}
						}
						// Makes sure that the right stone is dropped
						if (block.getTypeId() == 1 && block.getData() != (byte) 0) {
							return;
						}

						if (tool.containsEnchantment(Enchantment.SILK_TOUCH)) {
							// Checks for Silktouch & and allows for Silktouch
							// items to override default
							if (plugin.getConfigManager().silkTouch) {
								if (block.getType() == Material.STONE) {
									item.setAmount(item.getAmount() - 1);
									item1 = new ItemStack(Material.STONE, 1);
								} else if (block.getType() == Material.DIAMOND_ORE) {
									item.setAmount(item.getAmount() - 1);
									item1 = new ItemStack(Material.DIAMOND_ORE, 1);
								} else if (block.getType() == Material.COAL_ORE) {
									item.setAmount(item.getAmount() - 1);
									item1 = new ItemStack(Material.COAL_ORE, 1);
								} else if (block.getType() == Material.REDSTONE_ORE) {
									item.setAmount(item.getAmount() - 1);
									item1 = new ItemStack(Material.REDSTONE_ORE, 1);
								} else if (block.getType() == Material.GRASS) {
									item.setAmount(item.getAmount() - 1);
									item1 = new ItemStack(Material.GRASS, 1);
								} else if (block.getType() == Material.LAPIS_ORE) {
									item.setAmount(item.getAmount() - 1);
									item1 = new ItemStack(Material.LAPIS_ORE, 1);
								}
							}
						}

						// Checks for Fortune tools and adds it to the
						// Dwarfcraft drops
						Material type = block.getType();
						if (type == Material.DIAMOND_ORE || type == Material.COAL_ORE || type == Material.REDSTONE_ORE || type == Material.GRASS || type == Material.STONE || type == Material.LAPIS_ORE) {
							if (tool.containsEnchantment(Enchantment.LOOT_BONUS_BLOCKS)) {
								int lvl = tool.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS);
								Random r = new Random();
								int num = r.nextInt(99) + 1;
								switch (lvl) {
								case 1:
									if (1 <= num && num <= 33) {
										item.setAmount(item.getAmount() + 1);
									}
									break;
								case 2:
									if (1 <= num && num <= 25) {
										item.setAmount(item.getAmount() + 1);
									} else if (26 <= num && num <= 50) {
										item.setAmount(item.getAmount() + 2);
									}
									break;
								case 3:
									if (1 <= num && num <= 20) {
										item.setAmount(item.getAmount() + 1);
									} else if (21 <= num && num <= 40) {
										item.setAmount(item.getAmount() + 2);
									} else if (41 <= num && num <= 60) {
										item.setAmount(item.getAmount() + 2);
									}
									break;
								default:
									break;
								}
							}
						}

						if (DwarfCraft.debugMessagesThreshold < 6)
							System.out.println("Debug: dropped " + item.toString());

						if (item.getAmount() > 0)
							loc.getWorld().dropItemNaturally(loc, item);

						if (item1 != null) {
							loc.getWorld().dropItemNaturally(loc, item1);
						}
						if (event.getExpToDrop() != 0) {
							((ExperienceOrb) loc.getWorld().spawn(loc, ExperienceOrb.class)).setExperience(event.getExpToDrop());
						}
						if (plugin.getConsumer() != null) {
							plugin.getConsumer().queueBlockBreak(event.getPlayer().getName(), event.getBlock().getState());
						}
						blockDropChange = true;

					}
				}
			}
		}

		if (tool != null && tool.getType().getMaxDurability() > 0) {
			for (Skill s : skills.values()) {
				for (Effect e : s.getEffects()) {
					if (e.getEffectType() == EffectType.SWORDDURABILITY && e.checkTool(tool))
						e.damageTool(player, 2, tool, !blockDropChange);

					if (e.getEffectType() == EffectType.TOOLDURABILITY && e.checkTool(tool))
						e.damageTool(player, 1, tool, !blockDropChange);
				}
			}
		}

		if (blockDropChange) {
			event.getBlock().setTypeId(0);
			event.setCancelled(true);
		}
	}

	/**
	 * onBlockDamage used to accelerate how quickly blocks are destroyed.
	 * setDamage() not implemented yet
	 */

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockDamage(BlockDamageEvent event) {

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

		Player player = event.getPlayer();
		DCPlayer dCPlayer = plugin.getDataManager().find(player);
		HashMap<Integer, Skill> skills = dCPlayer.getSkills();

		// Effect Specific information
		ItemStack tool = player.getItemInHand();
		int materialId = event.getBlock().getTypeId();
		byte data = event.getBlock().getData();

		// if (event.getDamageLevel() != BlockDamageLevel.STARTED)
		// return;

		for (Skill s : skills.values()) {
			for (Effect e : s.getEffects()) {
				if (e.getEffectType() == EffectType.DIGTIME && e.checkInitiator(materialId, data) && e.checkTool(tool)) {
					if (DwarfCraft.debugMessagesThreshold < 2)
						System.out.println("DC2: started instamine check");

					if (Util.randomAmount(e.getEffectAmount(dCPlayer)) == 0)
						return;

					if (DwarfCraft.debugMessagesThreshold < 3)
						System.out.println("DC3: Insta-mine occured. Block: " + materialId);

					event.setInstaBreak(true);
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockPhysics(BlockPhysicsEvent event) {
		if (event.getBlock().getType() == Material.CACTUS && plugin.getConfigManager().disableCacti) {

			if (plugin.getConfigManager().worldBlacklist) {
				for (World w : plugin.getConfigManager().worlds) {
					if (w != null) {
						if (event.getBlock().getWorld() == w) {
							return;
						}
					}
				}
			}

			World world = event.getBlock().getWorld();
			Location loc = event.getBlock().getLocation();
			// this is a re-implementation of BlockCactus's doPhysics event,
			// minus the spawning of a droped item.
			if (!(checkCacti(world, loc))) {
				event.getBlock().setTypeId(0, true);
				event.setCancelled(true);

				Material base = world.getBlockAt(loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ()).getType();
				if ((base != Material.CACTUS) && (base != Material.SAND))
					world.dropItemNaturally(loc, new ItemStack(Material.CACTUS, 1));

			}
		}
	}

	private boolean checkCacti(World world, Location loc) {
		int x = loc.getBlockX();
		int y = loc.getBlockY();
		int z = loc.getBlockZ();

		if (isBuildable(world.getBlockAt(x - 1, y, z).getType()))
			return false;
		if (isBuildable(world.getBlockAt(x + 1, y, z).getType()))
			return false;
		if (isBuildable(world.getBlockAt(x, y, z - 1).getType()))
			return false;
		if (isBuildable(world.getBlockAt(x, y, z + 1).getType()))
			return false;

		Material base = world.getBlockAt(x, y - 1, z).getType();

		return (base == Material.CACTUS) || (base == Material.SAND);
	}

	// Bukkit really needs to implement access to Material.isBuildable()
	private boolean isBuildable(Material block) {
		switch (block) {
		case AIR:
		case WATER:
		case STATIONARY_WATER:
		case LAVA:
		case STATIONARY_LAVA:
		case YELLOW_FLOWER:
		case RED_ROSE:
		case BROWN_MUSHROOM:
		case RED_MUSHROOM:
		case SAPLING:
		case SUGAR_CANE:
		case FIRE:
		case STONE_BUTTON:
		case DIODE_BLOCK_OFF:
		case DIODE_BLOCK_ON:
		case LADDER:
		case LEVER:
		case RAILS:
		case REDSTONE_WIRE:
		case TORCH:
		case REDSTONE_TORCH_ON:
		case REDSTONE_TORCH_OFF:
		case SNOW:
		case POWERED_RAIL:
		case DETECTOR_RAIL:
			return false;
		default:
			return true;
		}
	}

}
