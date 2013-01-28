package com.Jessy1237.DwarfCraft;

/**
 * Original Authors: smartaleq, LexManos and RCarretta
 */

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

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

	public static ItemStack parseItem(String info) {
		String[] pts = info.split(":");
		int data = (pts.length == 1 ? -1 : Integer.parseInt(pts[1]));
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
		item1.setData(new MaterialData(item1.getTypeId(), (data == -1 ? (byte) -1 : (byte) (data & 0xFF))));
		return item1;
	}

	public static String getCleanName(ItemStack item) {
		if (item == null)
			return "NULL";
		if (item.getData() == null || item.getData().getData() == -1)
			return item.getType().toString();

		switch (item.getType()) {
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
		case SAPLING:
			switch (item.getData().getData()) {
			case 0:
				return "Oak Sapling";
			case 1:
				return "Spruce Sapling";
			case 2:
				return "Birch Sapling";
			case 3:
				return "Jungle Tree Sapling";
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
			}
		case DOUBLE_STEP:
			switch (item.getData().getData()) {
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
				return String.format("Unknown Dye(%d)", item.getData().getData());
			}
		case CROPS:
			switch (item.getData().getData()) {
			case 7:
				return "Fully Grown Crops";
			default:
				return String.format("Unknown Dye(%d)", item.getData().getData());
			}
		case COAL:
			switch (item.getData().getData()) {
			case 0:
				return "Coal";
			case 1:
				return "Charcoal";
			}
		default:
			return item.getType().toString();
		}
	}
}
