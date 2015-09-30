package com.Jessy1237.DwarfCraft;

/**
 * Original Authors: smartaleq, LexManos and RCarretta
 */

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class Util {

	private static DwarfCraft plugin;
	
	@SuppressWarnings("static-access")
	public Util(DwarfCraft plugin) {
		this.plugin = plugin;
	}
	
	// Stolen from nossr50
	private static int charLength(char x) {
		if ("i.:,;|!".indexOf(x) != -1)
			return 2;
		else if ("l'".indexOf(x) != -1)
			return 3;
		else if ("tI[]".indexOf(x) != -1)
			return 4;
		else if ("fk{}<>\"*()".indexOf(x) != -1)
			return 5;
		else if ("abcdeghjmnopqrsuvwxyzABCDEFGHJKLMNOPQRSTUVWXYZ1234567890\\/#?$%-=_+&^".indexOf(x) != -1)
			return 6;
		else if ("@~".indexOf(x) != -1)
			return 7;
		else if (x == ' ')
			return 4;
		else
			return -1;
	}

	protected static int msgLength(String str) {
		int len = 0;

		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) == '&') {
				i++;
				continue; // increment by 2 for colors, as in the case of "&3"
			}
			len += charLength(str.charAt(i));
		}
		return len;
	}

	public static int randomAmount(double input) {
		double rand = Math.random();
		if (rand > input % 1)
			return (int) Math.floor(input);
		else
			return (int) Math.ceil(input);
	}

	protected static String sanitize(String str) {
		String retval = "";
		for (int i = 0; i < str.length(); i++) {
			if ("abcdefghijlmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890_".indexOf(str.charAt(i)) != -1)
				retval = retval + str.charAt(i);
		}
		return retval;
	}

	@SuppressWarnings("deprecation")
	public static ItemStack parseItem(String info) {
		String[] pts = info.split(":");
		int data = (pts.length > 1 ? Integer.parseInt(pts[1]) : 0);
		int item = -1;

		try {
			item = Integer.parseInt(pts[0]);
		} catch (NumberFormatException e) {
			Material mat = Material.getMaterial(pts[0]);
			if (mat == null) {
				System.out.println("DC ERROR: Could not parse material: " + info);
				return null;
			}
			item = mat.getId();
		}
		ItemStack item1 = new ItemStack(item);
		item1.setDurability((short) data);
		return item1;
	}

	@SuppressWarnings("deprecation")
	public static String getCleanName(ItemStack item) {
		if (item == null)
			return "NULL";
		switch (item.getType()) {
		case SAPLING:
			if(Util.checkEquivalentBuildBlocks(item.getTypeId(), -1) != null)
				return "Sapling";
			switch (item.getData().getData()) {
			case 0:
				return "Oak Sapling";
			case 1:
				return "Spruce Sapling";
			case 2:
				return "Birch Sapling";
			case 3:
				return "Jungle Sapling";
			case 4:
				return "Acacia Sapling";
			case 5:
				return "Dark Oak Sapling";
			default:
				return "Sapling";
			}
		case SAND:
			switch (item.getData().getData()) {
			case 0:
				return "Sand";
			case 1:
				return "Red Sand";
			default:
				return "Sand";
			}
		case RAW_FISH:
			if(Util.checkEquivalentBuildBlocks(item.getTypeId(), -1) != null)
				return "Raw Fish";
			switch (item.getData().getData()) {
			case 0:
				return "Raw Fish";
			case 1:
				return "Raw Salmon";
			case 2:
				return "Clownfish";
			case 3:
				return "Pufferfish";
			default:
				return "Raw Fish";
			}

		case LOG:
			if(Util.checkEquivalentBuildBlocks(item.getTypeId(), -1) != null)
				return "Log";
			switch (item.getData().getData()) {
			case 0:
				return "Oak Log";
			case 1:
				return "Spruce Log";
			case 2:
				return "Birch Log";
			case 3:
				return "Jungle Tree Log";
			default:
				return "Log";
			}
		case LOG_2:
			if(Util.checkEquivalentBuildBlocks(item.getTypeId(), -1) != null)
				return "Log";
			switch (item.getData().getData()) {
			case 0:
				return "Acacia Log";
			case 1:
				return "Dark Oak Log";
			default:
				return "Log";
			}
		case LEAVES:
			if(Util.checkEquivalentBuildBlocks(item.getTypeId(), -1) != null)
				return "Leaves";
			switch (item.getData().getData()) {
			case 0:
				return "Oak Leaves";
			case 1:
				return "Spruce Leaves";
			case 2:
				return "Birch Leaves";
			case 3:
				return "Jungle Tree Leaves";
			default:
				return "Leaves";
			}
		case LEAVES_2:
			if(Util.checkEquivalentBuildBlocks(item.getTypeId(), -1) != null)
				return "Leaves";
			switch (item.getData().getData()) {
			case 0:
				return "Acacia Leaves";
			case 1:
				return "Dark Oak Leaves";
			default:
				return "Leaves";
			}
		case WOOL:
			if(Util.checkEquivalentBuildBlocks(item.getTypeId(), -1) != null)
				return "Wool";
			switch (item.getData().getData()) {
			case 0:
				return "White Wool";
			case 1:
				return "Orange Dye";
			case 2:
				return "Magenta Dye";
			case 3:
				return "Light Blue Dye";
			case 4:
				return "Dandelion Yellow";
			case 5:
				return "Lime Dye";
			case 6:
				return "Pink Dye";
			case 7:
				return "Gray Dye";
			case 8:
				return "Light Gray Dye";
			case 9:
				return "Cyan Dye";
			case 10:
				return "Purple Dye";
			case 11:
				return "Lapis Lazuli";
			case 12:
				return "Cocoa Beans";
			case 13:
				return "Cactus Green";
			case 14:
				return "Rose Red";
			case 15:
				return "Ink Sac";
			default:
				return String.format("Unknown Dye(%d)", item.getData().getData());
			}
		case DOUBLE_STEP:
			if(Util.checkEquivalentBuildBlocks(item.getTypeId(), -1) != null)
				return "Slab";
			switch (item.getData().getData()) {
			case 15:
				return "Tile Quartz Double Slab";
			case 9:
				return "Smooth Sandstone Double Slab";
			case 8:
				return "Smooth Stone Double Slab";
			case 7:
				return "Quarts Double Slab";
			case 6:
				return "Nether Brick Double Slab";
			case 5:
				return "Stone Brick Double Slab";
			case 4:
				return "Brick Double Slab";
			case 3:
				return "Cobblestone Double Slab";
			case 2:
				return "Wooden Double Slab";
			case 1:
				return "Sandstone Double Slab";
			case 0:
				return "Stone Double Slab";
			default:
				return String.format("Slab");
			}
		case SUGAR_CANE_BLOCK:
			return "Sugar Cane";
		case CROPS:
			switch (item.getData().getData()) {
			case 7:
				return "Fully Grown Crops";
			default:
				return String.format("Crop");
			}
		case COAL:
			if(Util.checkEquivalentBuildBlocks(item.getTypeId(), -1) != null)
				return "Coal";
			switch (item.getData().getData()) {
			case 0:
				return "Coal";
			case 1:
				return "Charcoal";
			default:
				return "Coal";
			}
		case SULPHUR:
			return "Gun Powder";
		case NETHER_STALK:
			return "Nether Wart";
		case NETHER_WARTS:
			return "Nether Wart";
		case POTATO_ITEM:
			return "Potato";
		case POTATO:
			return "Potato_Crop";
		case CARROT_ITEM:
			return "Carrot";
		case CARROT:
			return "Carrot_Crop";
		case INK_SACK:
			if(Util.checkEquivalentBuildBlocks(item.getTypeId(), -1) != null)
				return "Dye";
			switch (item.getData().getData()) {
			case 15:
				return "Bone Meal";
			case 14:
				return "Orange Dye";
			case 13:
				return "Magenta Dye";
			case 12:
				return "Light Blue Dye";
			case 11:
				return "Dandelion Yellow";
			case 10:
				return "Lime Dye";
			case 9:
				return "Pink Dye";
			case 8:
				return "Gray Dye";
			case 7:
				return "Light Gray Dye";
			case 6:
				return "Cyan Dye";
			case 5:
				return "Purple Dye";
			case 4:
				return "Lapis Lazuli";
			case 3:
				return "Cocoa Beans";
			case 2:
				return "Cactus Green";
			case 1:
				return "Rose Red";
			case 0:
				return "Ink Sac";
			default:
				return String.format("Unknown Dye(%d)", item.getData().getData());
			}
		default:
			return item.getType().toString();
		}
	}

	// Checks the itemID to see if it is a tool. Excludes fishing rod and, flint
	// and steel.
	public static boolean isTool(int ID) {
		if ((ID >= 256 && ID <= 258) || (ID >= 267 && ID <= 279) || (ID >= 283 && ID <= 286) || (ID >= 290 && ID <= 294) || ID == 359) {
			return true;
		}
		return false;
	}
	
	
	//Checks the EquivalentBlock Groups of the ID and the compareID, returns the group of blocks that are equivalent if the compareID = -1
	public static ArrayList<Integer> checkEquivalentBuildBlocks(int ID, int compareID) {	
		if(!plugin.getConfigManager().buildingblocks)
			return null;
		
		for(ArrayList<Integer> blocks : plugin.getConfigManager().getBlockGroups().values()) {
			if(blocks != null && blocks.size() > 0) {
				for(Integer i : blocks) {
					if(ID == i) {
						for(Integer i1 : blocks) {
							if(compareID == i1 || compareID == -1) {
								return blocks;
							}
						}
					}
				}
			}
		}
		
		return null;
	}

	@SuppressWarnings("deprecation")
	public static String getCleanName(EntityType mCreature) {
		if (mCreature == null)
			return "NULL";

		switch (mCreature) {
		case MUSHROOM_COW:
			return "Mooshroom";
		case IRON_GOLEM:
			return "Iron Golem";
		case MAGMA_CUBE:
			return "Magma Cube";
		case ENDER_DRAGON:
			return "Ender Dragon";
		case WITHER_SKULL:
			return "Wither Skull";
		case PIG_ZOMBIE:
			return "Pig Zombie";
		case CAVE_SPIDER:
			return "Cave Spider";
		case WITHER:
			return "Wither";
		case OCELOT:
			return "Ocelot";
		default:
			return mCreature.getName();
		}
	}

	public enum FoodLevel {

		APPLE(260, 4, 2.4f),
		BAKED_POTATO(393, 5, 7.2f),
		BREAD(297, 5, 6f),
		CAKE(92, 2, 2f),
		CARROT(391, 3, 4.8f),
		COOKED_CHICKEN(366, 6, 7.2f),
		COOKED_FISH(350, 5, 6f),
		COOKED_MUTTON(424, 6, 9.6f),
		COOKED_PORKCHOP(320, 8, 12.8f),
		COOKED_RABBIT(412, 5, 6f),
		COOKIE(357, 2, 0.4f),
		GOLDEN_APPLE(322, 4, 9.6f),
		GOLDEN_CARROT(396, 6, 14.4f),
		MELON(360, 2, 1.2f),
		MUSHROOM_STEW(282, 6, 7.2f), 
		POISONOUS_POTATO(394, 2, 1.2f),
		POTATO(392, 1, 0.6f),
		PUMPKIN_PIE(400, 8, 4.8f),
		RABBIT_STEW(413, 10, 12f),
		RAW_BEEF(363, 3, 1.8f),
		RAW_CHICKEN(365, 2, 1.2f),
		RAW_FISH(349, 2, 0.4f),
		RAW_MUTTON(423, 2, 1.2f),
		RAW_PORKCHOP(319, 3, 1.8f),
		RAW_RABBIT(411, 2, 1.8f),
		ROTTEN_FLESH(367, 4, 0.8f),
		SPIDER_EYE(375, 2, 3.2f),
		STEAK(364, 8, 12.8f)
		;

		private int lvl;
		private int id;
		private float sat;

		private FoodLevel(int id, int lvl, float sat) {
			this.id = id;
			this.lvl = lvl;
			this.sat = sat;
		}

		public int getId() {
			return id;
		}

		public int getLevel() {
			return this.lvl;
		}

		public float getSat() {
			return this.sat;
		}

		public static int getLvl(int id) {
			for (FoodLevel f : FoodLevel.values()) {
				if (f != null) {
					if (f.getId() == id) {
						return f.getLevel();
					}
				}
			}
			return 0;
		}

		public static float getSat(int id) {
			for (FoodLevel f : FoodLevel.values()) {
				if (f != null) {
					if (f.getId() == id) {
						return f.getSat();
					}
				}
			}
			return 0;
		}
	}
	
	public static boolean isWorldAllowed(World world) {
		if (plugin.getConfigManager().worldBlacklist) {
			for (World w : plugin.getConfigManager().worlds) {
				if (w != null) {
					if (world.equals(w)) {
						return false;
					}
				}
			}
		}
		
		return true;
	}
}
