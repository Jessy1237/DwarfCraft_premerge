package com.Jessy1237.DwarfCraft.events;

/**
 * Original Authors: smartaleq, LexManos and RCarretta
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import com.Jessy1237.DwarfCraft.DCPlayer;
import com.Jessy1237.DwarfCraft.DwarfCraft;
import com.Jessy1237.DwarfCraft.Effect;
import com.Jessy1237.DwarfCraft.EffectType;
import com.Jessy1237.DwarfCraft.Skill;
import com.Jessy1237.DwarfCraft.Util;

public class DCBlockListener implements Listener {
	private final DwarfCraft plugin;
	private HashMap<Block, Player> crops = new HashMap<Block, Player>();

	public DCBlockListener(final DwarfCraft plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockFromTo(BlockFromToEvent event) {
		if (plugin.getConfigManager().disableCacti) {
			if (!Util.isWorldAllowed(event.getBlock().getWorld()))
				return;

			// Code to prevent water from normally breaking crops
			// Added due to players being able to bypass DC skill restrictions
			if (event.getToBlock().getType() == Material.CROPS || event.getToBlock().getType() == Material.POTATO || event.getToBlock().getType() == Material.CARROT || event.getToBlock().getType() == Material.SUGAR_CANE_BLOCK || event.getToBlock().getType() == Material.CACTUS
					|| event.getToBlock().getType() == Material.COCOA || event.getToBlock().getType() == Material.NETHER_WARTS && (event.getBlock().getType() == Material.WATER || event.getBlock().getType() == Material.STATIONARY_WATER)) {
				event.getToBlock().setType(Material.AIR, true);
				event.setCancelled(true);
			}
		}
	}

	// Checks when a cactus grows to see if it trying to my auto farmed
	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockGrow(BlockGrowEvent event) {
		if (plugin.getConfigManager().disableCacti) {
			if (!Util.isWorldAllowed(event.getBlock().getWorld()))
				return;

			Block b = event.getNewState().getBlock();
			if (b.getType() == Material.CACTUS) {
				if (!checkCacti(b.getWorld(), b.getLocation())) {
					b.setType(Material.AIR);
					event.setCancelled(true);
				}
			} else {
				b = event.getBlock();
				if (!checkCacti(b.getWorld(), b.getLocation())) {
					b.setType(Material.AIR);
					event.setCancelled(true);
				} else {
					Location l = b.getLocation();
					l.setY(l.getBlockY() + 1);
					if (!checkCacti(b.getWorld(), l)) {
						b.getWorld().getBlockAt(l).setType(Material.AIR);
						event.setCancelled(true);
					}
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockBreak(BlockBreakEvent event) {
		if (!Util.isWorldAllowed(event.getPlayer().getWorld()))
			return;

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

		// Changed illuminated redstone ore block id to normal redstone ore
		// block id
		if (blockID == 74) {
			blockID = 73;
			block.setType(Material.REDSTONE_ORE);
		}

		boolean blockDropChange = false;
		for (Skill s : skills.values()) {
			for (Effect effect : s.getEffects()) {
				if (effect.getEffectType() == EffectType.BLOCKDROP && effect.checkInitiator(blockID, meta)) {

					// Crops special line:
					if (effect.getInitiatorId() == 59 || effect.getInitiatorId() == 141 || effect.getInitiatorId() == 142) {
						if (meta != 7)
							return;
					}

					if (effect.getInitiatorId() == 127) {
						if (meta < 8)
							return;
					}

					if (effect.getInitiatorId() == 115) {
						if (meta != 3)
							return;
					}

					// Checks for cactus/sugar cane blocks above the one broken
					// to apply the dwarfcraft blocks in the block physics
					// event.
					if (block.getType() == Material.CACTUS) {
						for (int i = 1; block.getWorld().getBlockAt(block.getX(), block.getY() + i, block.getZ()).getType() == Material.CACTUS; i++) {
							crops.put(block.getWorld().getBlockAt(block.getX(), block.getY() + i, block.getZ()), event.getPlayer());
						}
					}

					if (block.getType() == Material.SUGAR_CANE_BLOCK) {
						for (int i = 1; block.getWorld().getBlockAt(block.getX(), block.getY() + i, block.getZ()).getType() == Material.SUGAR_CANE_BLOCK; i++) {
							crops.put(block.getWorld().getBlockAt(block.getX(), block.getY() + i, block.getZ()), event.getPlayer());
						}
					}

					if (effect.checkTool(tool)) {
						ItemStack item = effect.getOutput(player, meta, blockID);
						ItemStack item1 = null;

						// Gives the 2% to drop poisonous potatoes when potatoes
						// are broken
						if (effect.getInitiatorId() == 142 && item.getTypeId() == 392) {
							Random r = new Random();
							final int i = r.nextInt(100);
							if (i == 0 || i == 1) {
								loc.getWorld().dropItemNaturally(loc, new ItemStack(394, 1));
							}
						}

						if (item.getTypeId() != 351 && item.getTypeId() == blockID && item.getTypeId() != 295 && blockID != 141 && item.getTypeId() != 391 && blockID != 142 && item.getTypeId() != 392 && blockID != 115 && item.getTypeId() != 372 && blockID != 31 && blockID != 175 && blockID != 59
								&& blockID != 105 && item.getTypeId() != 362 && blockID != 104 && item.getTypeId() != 361 && blockID != 127) {
							item.setDurability(block.getData());
						} else if (item.getTypeId() != 351) {
							item.setDurability(new ItemStack(item.getTypeId(), 1).getDurability());
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
						} else if (event.getBlock().getTypeId() == 162) {
							final ItemStack old = item;
							item = new ItemStack(Material.LOG_2, old.getAmount());
							if (block.getData() == 0 || block.getData() == 4 || block.getData() == 8 || block.getData() == 12) {
								item.setDurability((short) 0);
							}
							if (block.getData() == 1 || block.getData() == 5 || block.getData() == 9 || block.getData() == 13) {
								item.setDurability((short) 1);
							}

						}

						// Makes sure that the right stone is dropped
						if (block.getTypeId() == 1 && block.getData() != (byte) 0) {
							return;
						}

						if (tool.containsEnchantment(Enchantment.SILK_TOUCH)) {
							// If enabled in the config, silk touch block
							// replaces one of the items drop in the stack, if
							// not acts as vanilla and no DC drops
							if (plugin.getConfigManager().silkTouch) {
								item.setAmount(item.getAmount() - Util.randomAmount(effect.getEffectAmount(effect.getNormalLevel(), player)));
								switch (block.getType()) {
								case STONE:
									item1 = new ItemStack(Material.STONE, 1);
									break;
								case DIAMOND_ORE:
									item1 = new ItemStack(Material.DIAMOND_ORE, 1);
									break;
								case EMERALD_ORE:
									item1 = new ItemStack(Material.EMERALD_ORE, 1);
									break;
								case QUARTZ_ORE:
									item1 = new ItemStack(Material.QUARTZ_ORE, 1);
									break;
								case COAL_ORE:
									item1 = new ItemStack(Material.COAL_ORE, 1);
									break;
								case REDSTONE_ORE:
									item1 = new ItemStack(Material.REDSTONE_ORE, 1);
									break;
								case GLOWSTONE:
									item1 = new ItemStack(Material.GLOWSTONE, 1);
									break;
								case GRASS:
									item1 = new ItemStack(Material.GRASS, 1);
									break;
								case LAPIS_ORE:
									item1 = new ItemStack(Material.LAPIS_ORE, 1);
									break;
								default:
									break;
								}
							} else {
								switch (block.getType()) {
								case STONE:
									item = new ItemStack(Material.STONE, 1);
									break;
								case DIAMOND_ORE:
									item = new ItemStack(Material.DIAMOND_ORE, 1);
									break;
								case EMERALD_ORE:
									item = new ItemStack(Material.EMERALD_ORE, 1);
									break;
								case QUARTZ_ORE:
									item = new ItemStack(Material.QUARTZ_ORE, 1);
									break;
								case COAL_ORE:
									item = new ItemStack(Material.COAL_ORE, 1);
									break;
								case REDSTONE_ORE:
									item = new ItemStack(Material.REDSTONE_ORE, 1);
									break;
								case GLOWSTONE:
									item = new ItemStack(Material.GLOWSTONE, 1);
									break;
								case GRASS:
									item = new ItemStack(Material.GRASS, 1);
									break;
								case LAPIS_ORE:
									item = new ItemStack(Material.LAPIS_ORE, 1);
									break;
								default:
									break;
								}
							}
						}

						// Checks for Fortune tools and adds it to the
						// Dwarfcraft drops
						Material type = block.getType();
						if (type == Material.DIAMOND_ORE || type == Material.COAL_ORE || type == Material.REDSTONE_ORE || type == Material.EMERALD_ORE || type == Material.QUARTZ_ORE || type == Material.GRASS || type == Material.STONE || type == Material.LAPIS_ORE || type == Material.GLOWSTONE) {
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
		if (!Util.isWorldAllowed(event.getPlayer().getWorld()))
			return;

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

	// Code to check for farm automation i.e. (breaking the
	// block below, cacti farms, etc)
	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockPhysics(BlockPhysicsEvent event) {
		if (plugin.getConfigManager().disableCacti) {
			if (!Util.isWorldAllowed(event.getBlock().getWorld()))
				return;

			if (event.getBlock().getType() == Material.CROPS || event.getBlock().getType() == Material.POTATO || event.getBlock().getType() == Material.CARROT) {

				World world = event.getBlock().getWorld();
				Location loc = event.getBlock().getLocation();
				if (!(checkCrops(world, loc))) {
					event.getBlock().setTypeId(0, true);
					event.setCancelled(true);
				}
			} else if (event.getBlock().getType() == Material.CACTUS) {

				World world = event.getBlock().getWorld();
				Location loc = event.getBlock().getLocation();
				if (!(checkCacti(world, loc))) {
					int x = loc.getBlockX();
					int y = loc.getBlockY();
					int z = loc.getBlockZ();

					boolean remove = false;
					boolean checked = false;
					ArrayList<Block> removal = new ArrayList<Block>();
					for (Block b : crops.keySet()) {
						if (b != null) {
							if (b.getX() == x && b.getY() == y && b.getZ() == z) {
								DCPlayer dCPlayer = plugin.getDataManager().find(crops.get(b));
								for (Skill s : dCPlayer.getSkills().values()) {
									for (Effect e : s.getEffects()) {
										if (e.getEffectType() == EffectType.BLOCKDROP && e.checkInitiator(new ItemStack(Material.CACTUS))) {
											int amount = Util.randomAmount(e.getEffectAmount(dCPlayer));
											if (amount != 0) {
												world.dropItemNaturally(loc, new ItemStack(Material.CACTUS, amount));
											}
										}
									}
								}
								removal.add(b);
								remove = true;
								checked = true;
							}
						}
					}

					for (Block b : removal) {
						if (b != null) {
							crops.remove(b);
						}
					}
					if (remove) {
						event.getBlock().setTypeId(0, true);
						event.setCancelled(true);
					} else if (!checked) {
						event.getBlock().setTypeId(0, true);
						event.setCancelled(true);
					}
				}
			} else if (event.getBlock().getType() == Material.SUGAR_CANE_BLOCK) {
				World world = event.getBlock().getWorld();
				Location loc = event.getBlock().getLocation();
				int x = loc.getBlockX();
				int y = loc.getBlockY();
				int z = loc.getBlockZ();

				boolean remove = false;
				ArrayList<Block> removal = new ArrayList<Block>();
				for (Block b : crops.keySet()) {
					if (b != null) {
						if (b.getX() == x && b.getY() == y && b.getZ() == z) {
							DCPlayer dCPlayer = plugin.getDataManager().find(crops.get(b));
							for (Skill s : dCPlayer.getSkills().values()) {
								for (Effect e : s.getEffects()) {
									if (e.getEffectType() == EffectType.BLOCKDROP && e.checkInitiator(new ItemStack(Material.SUGAR_CANE_BLOCK))) {
										int amount = Util.randomAmount(e.getEffectAmount(dCPlayer));
										if (amount != 0) {
											world.dropItemNaturally(loc, new ItemStack(Material.SUGAR_CANE, amount));
										}
									}
								}
							}
							removal.add(b);
						}
					}
				}

				for (Block b : removal) {
					if (b != null) {
						crops.remove(b);
					}
				}
				if (remove) {
					event.getBlock().setTypeId(0, true);
					event.setCancelled(true);
				}
			}
		}
	}

	// Checks to see if pistons are breaking crop related blocks
	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockPistonExtend(BlockPistonExtendEvent event) {
		if (plugin.getConfigManager().disableCacti) {
			if (!Util.isWorldAllowed(event.getBlock().getWorld()))
				return;

			Material[] mats = { Material.COCOA, Material.CACTUS, Material.CROPS, Material.POTATO, Material.CARROT, Material.NETHER_STALK, Material.MELON_BLOCK, Material.SUGAR_CANE_BLOCK };
			if (removeCrops(event.getBlocks(), mats))
				event.setCancelled(true);
		}
	}

	// Checks to see if pistons are breaking crop related blocks
	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockPistonRetract(BlockPistonRetractEvent event) {
		if (plugin.getConfigManager().disableCacti) {
			if (!Util.isWorldAllowed(event.getBlock().getWorld()))
				return;

			Material[] mats = { Material.COCOA, Material.CACTUS, Material.CROPS, Material.POTATO, Material.CARROT, Material.NETHER_WARTS, Material.MELON_BLOCK, Material.SUGAR_CANE_BLOCK };
			if (removeCrops(event.getBlocks(), mats))
				event.setCancelled(true);
		}
	}

	private boolean removeCrops(List<Block> blocks, Material[] mats) {
		boolean bool = false;
		for (Material m : mats) {
			if (m != null) {
				for (Block b : blocks) {
					if (b != null) {
						if (b.getType() == m) {
							b.setType(Material.AIR, true);
							bool = true;
						} else if (m == Material.COCOA) {
							if (b.getRelative(BlockFace.SOUTH).getType() == m) {
								b.getRelative(BlockFace.SOUTH).setType(Material.AIR);
								bool = true;
							} else if (b.getRelative(BlockFace.NORTH).getType() == m) {
								b.getRelative(BlockFace.NORTH).setType(Material.AIR);
								bool = true;
							} else if (b.getRelative(BlockFace.WEST).getType() == m) {
								b.getRelative(BlockFace.WEST).setType(Material.AIR);
								bool = true;
							} else if (b.getRelative(BlockFace.EAST).getType() == m) {
								b.getRelative(BlockFace.EAST).setType(Material.AIR);
								bool = true;
							}
						}
					}
				}
			}
		}
		return bool;
	}

	private boolean checkCrops(World world, Location loc) {
		int x = loc.getBlockX();
		int y = loc.getBlockY();
		int z = loc.getBlockZ();

		Material base = world.getBlockAt(x, y - 1, z).getType();

		return (base == Material.SOIL);
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
