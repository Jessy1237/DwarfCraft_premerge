package com.Jessy1237.DwarfCraft;

/**
 * Original Authors: smartaleq, LexManos and RCarretta
 */

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Util {

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
			switch (item.getData().getData()) {
			case 0:
				return "Acacia Log";
			case 1:
				return "Dark Oak Log";
			default:
				return "Log";
			}
		case LEAVES:
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
			switch (item.getData().getData()) {
			case 0:
				return "Acacia Leaves";
			case 1:
				return "Dark Oak Leaves";
			default:
				return "Leaves";
			}
		case WOOL:
			switch (item.getData().getData()) {
			case 0:
				return "White Wool";
			case 1:
				return "Orange Wool";
			case 2:
				return "Magenta Wool";
			case 3:
				return "Light Blue Wool";
			case 4:
				return "Yellow Wool";
			case 5:
				return "Lime Wool";
			case 6:
				return "Pink Wool";
			case 7:
				return "Gray Wool";
			case 8:
				return "Light Gray Wool";
			case 9:
				return "Cyan Wool";
			case 10:
				return "Purple Wool";
			case 11:
				return "Blue Wool";
			case 12:
				return "Brown Wool";
			case 13:
				return "Green Wool";
			case 14:
				return "Red Wool";
			case 15:
				return "Black Wool";
			default:
				return "Wool";
			}
		case DOUBLE_STEP:
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
		case CROPS:
			switch (item.getData().getData()) {
			case 7:
				return "Fully Grown Crops";
			default:
				return String.format("Crop");
			}
		case COAL:
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
	public static boolean isTool(int i) {
		if ((i >= 256 && i <= 258) || (i >= 267 && i <= 279) || (i >= 283 && i <= 286) || (i >= 290 && i <= 294)) {
			return true;
		}
		return false;
	}
}
