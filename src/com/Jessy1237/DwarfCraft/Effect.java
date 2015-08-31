package com.Jessy1237.DwarfCraft;

/**
 * Original Authors: smartaleq, LexManos and RCarretta
 */

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.CaveSpider;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Cow;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Giant;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.Pig;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Silverfish;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Snowman;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Squid;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.jbls.LexManos.CSV.CSVRecord;

@SuppressWarnings("deprecation")
public class Effect {
	private int mID;
	private double mBase;
	private double mLevelIncrease;
	private double mLevelIncreaseNovice;
	private double mMin;
	private double mMax;
	private boolean mException;
	private double mExceptionLow;
	private double mExceptionHigh;
	private double mExceptionValue;
	private int mNormalLevel;
	private EffectType mType;
	private ItemStack mInitator;
	private ItemStack mOutput;
	private boolean mRequireTool;
	private int[] mTools;
	private boolean mFloorResult;

	private CreatureType mCreature;

	public Effect(CSVRecord record) {
		if (record == null)
			return;
		mID = record.getInt("ID");
		mBase = record.getDouble("BaseValue");
		mLevelIncrease = record.getDouble("LevelIncrease");
		mLevelIncreaseNovice = record.getDouble("LevelIncreaseNovice");
		mMin = record.getDouble("Min");
		mMax = record.getDouble("Max");
		mException = record.getBool("Exception");
		mExceptionLow = record.getInt("ExceptionLow");
		mExceptionHigh = record.getInt("ExceptionHigh");
		mExceptionValue = record.getDouble("ExceptionValue");
		mNormalLevel = record.getInt("NormalLevel");
		mType = EffectType.getEffectType(record.getString("Type"));
		if (mType != EffectType.MOBDROP) {
			mInitator = Util.parseItem(record.getString("OriginID"));
		} else {
			mCreature = CreatureType.fromName(record.getString("OriginID"));
		}
		mOutput = Util.parseItem(record.getString("OutputID"));
		mRequireTool = record.getBool("RequireTool");
		mFloorResult = record.getBool("Floor");

		if (record.getString("Tools").isEmpty())
			mTools = new int[0];
		else {
			String[] stools = record.getString("Tools").split(" ");
			mTools = new int[stools.length];
			for (int x = 0; x < stools.length; x++)
				mTools[x] = Integer.parseInt(stools[x]);
		}
	}

	/**
	 * General description of a benefit including minimum and maximum benefit
	 * 
	 * @return
	 */
	protected String describeGeneral() {
		String description;
		String initiator = Util.getCleanName(mInitator);
		if (initiator.equalsIgnoreCase("AIR"))
			initiator = "None";
		String output = Util.getCleanName(mOutput);
		if (output.equalsIgnoreCase("AIR"))
			output = "None";
		double effectAmountLow = getEffectAmount(0);
		double effectAmountHigh = getEffectAmount(30);
		double elfAmount = getEffectAmount(-1);
		String toolType = toolType();
		description = String.format("Effect Block Trigger: %s Block Output: %s . " + "Effect value ranges from %.2f - %.2f for levels 0 to 30. " + "Elves have the effect %.2f , as if they were level %d . " + "Tools affected: %s. " + (mRequireTool ? "Tool needed." : "Tool not needed."), initiator,
				output, effectAmountLow, effectAmountHigh, elfAmount, mNormalLevel, toolType);

		return description;
	}

	/**
	 * Description of a skills effect at a given level
	 * 
	 * @param dCPlayer
	 * @return
	 */
	protected String describeLevel(DCPlayer dCPlayer) {
		if (dCPlayer == null)
			return "Failed"; // TODO add failure code

		String description = "no skill description";
		// Variables used in skill descriptions
		String initiator = Util.getCleanName(mInitator);
		String output = Util.getCleanName(mOutput);

		double effectAmount = getEffectAmount(dCPlayer);
		double elfAmount = getEffectAmount(mNormalLevel);
		boolean moreThanOne = (effectAmount > 1);
		String effectLevelColor = effectLevelColor(dCPlayer.getSkill(this).getLevel());
		String toolType = toolType();

		switch (mType) {
		case BLOCKDROP:
			description = String.format("&6Break a &2%s &6and %s%.2f &2%s&6 are created", initiator, effectLevelColor, effectAmount, output);
			break;
		case MOBDROP:
			if (mID == 850 || mID == 851) {
				description = String.format("&6Zombies drop about %s%.2f &2%s", effectLevelColor, effectAmount, output);
				break;
			}
			description = String.format("&6Enemies that drop &2%s &6leave about %s%.2f&6", output, effectLevelColor, effectAmount, output);
			break;
		case SWORDDURABILITY:
			description = String.format("&6Using &2%s &6removes about %s%.2f &6durability", toolType, effectLevelColor, effectAmount);
			break;
		case PVPDAMAGE:
			description = String.format("&6You do %s%d&6%% &6of normal &2%s &6damage when fighting players", effectLevelColor, (int) (effectAmount * 100), toolType);
			break;
		case PVEDAMAGE:
			description = String.format("&6You do %s%d&6%% &6of normal &2%s &6damage when fighting mobs", effectLevelColor, (int) (effectAmount * 100), toolType);
			break;
		case EXPLOSIONDAMAGE:
			if (moreThanOne) {
				description = String.format("&6You take %s%d%% more &6damage from explosions", effectLevelColor, (int) (effectAmount * 100 - 100));
				break;
			} else {
				description = String.format("&6You take %s%d%% less &6damage from explosions", effectLevelColor, (int) (effectAmount * 100 - 100));
				break;
			}
		case FIREDAMAGE:
			if (moreThanOne) {
				description = String.format("&6You take %s%d%% more &6damage from fire", effectLevelColor, (int) (effectAmount * 100 - 100));
				break;
			} else {
				description = String.format("&6You take %s%d%% less &6damage from fire", effectLevelColor, (int) (effectAmount * 100 - 100));
				break;
			}
		case FALLDAMAGE:
			if (moreThanOne) {
				description = String.format("&6You take %s%d%% more &6damage from falling", effectLevelColor, (int) (effectAmount * 100 - 100));
				break;
			} else {
				description = String.format("&6You take %s%d%% less &6damage from falling", effectLevelColor, (int) (effectAmount * 100 - 100));
				break;
			}
		case FALLTHRESHOLD:
			description = String.format("&6Fall damage less than %s%d &6does not affect you.", effectLevelColor, (int) effectAmount);
			break;
		case PLOWDURABILITY:
			description = String.format("&6Using &2%s &6removes about %s%.2f &6durability", toolType, effectLevelColor, effectAmount);
			break;
		case TOOLDURABILITY:
			description = String.format("&6Using &2%s &6removes about %s%.2f &6durability", toolType, effectLevelColor, effectAmount);
			break;
		case RODDURABILITY:
			description = String.format("&6Using a &2%s &6removes about %s%.2f &6durability", toolType, effectLevelColor, effectAmount);
			break;
		case EAT:
			description = String.format("&6You gain %s%.2f &6Hunger (not &e%.2f&6) when you eat &2%s", effectLevelColor, effectAmount / 2, elfAmount / 2, initiator);
			break;
		case CRAFT:
			description = String.format("&6You craft %s%.0f &2%s &6instead of &e%.0f", effectLevelColor, effectAmount, output, elfAmount);
			break;
		case PLOW:
			description = String.format("&6You gain %s%.2f &6seeds instead of &e%.2f &6when you plow grass", effectLevelColor, effectAmount, elfAmount);
			break;
		case FISH:
			description = String.format("&6You catch %s%.2f &6fish instead of &e%.2f &6when you fish", effectLevelColor, effectAmount, elfAmount);
			break;
		case BREW:
			description = String.format("&6You brew %s%.2f &6potion(s) instead of &e%.2f &6when you're brewing potions", effectLevelColor, effectAmount, (float)mNormalLevel);
			break;
		case DIGTIME:
			description = String.format("&a%.0f%%&6 of the time &2%s &6break &2%s &6instantly ", effectAmount * 100, toolType, initiator);
			break;
		case BOWATTACK:
			description = String.format("&6Your Arrows (Fully Charge Bow) do %s%.0f &6hp damage (half hearts)", effectLevelColor, effectAmount + 2);
			break;
		case VEHICLEDROP:
			description = String.format("&6DISABLED: When you break a boat &6approx. %s%.2f &2%s&6 are created", effectLevelColor, effectAmount, output);
			break;
		case VEHICLEMOVE:
			description = String.format("&6Your boat travels %s%d%% &6faster than normal", effectLevelColor, (int) (effectAmount * 100 - 100));
			break;
		case SMELT:
			if (mInitator.getTypeId() == 265) {
				initiator = Util.getCleanName(new ItemStack(Material.IRON_ORE));
			} else if (mInitator.getTypeId() == 266) {
				initiator = Util.getCleanName(new ItemStack(Material.GOLD_ORE));
			}
			description = String.format("&6Smelt a &2%s &6and %s%.2f &2%s&6 are created as well", initiator, effectLevelColor, effectAmount, output);
			break;
		case SPECIAL:
		default:
			description = "&6This Effect description is not yet implemented: " + mType.toString();
		}

		return description;
	}

	private String effectLevelColor(int skillLevel) {
		if (skillLevel > mNormalLevel)
			return "&a";
		else if (skillLevel == mNormalLevel)
			return "&e";
		else
			return "&c";
	}

	/**
	 * Returns an effect Amount for a particular Dwarf
	 * 
	 * @param dCPlayer
	 * @return
	 */
	public double getEffectAmount(DCPlayer dCPlayer) {
		return getEffectAmount(dCPlayer.isElf() ? -1 : dCPlayer.skillLevel(this.mID / 10));
	}

	/**
	 * Used for getting the effect amount at a particular skill level. Where
	 * possible use getEffectAmount(Dwarf), which checks for Dwarf vs. Elf.
	 */
	private double getEffectAmount(int skillLevel) {
		double effectAmount = mBase;
		if (skillLevel == -1)
			skillLevel = mNormalLevel;
		effectAmount += skillLevel * mLevelIncrease;
		effectAmount += Math.min(skillLevel, 5) * mLevelIncreaseNovice;
		effectAmount = Math.min(effectAmount, mMax);
		effectAmount = Math.max(effectAmount, mMin);

		if (mException && skillLevel <= mExceptionHigh && skillLevel >= mExceptionLow)
			effectAmount = mExceptionValue;

		if (DwarfCraft.debugMessagesThreshold < 1) {
			System.out.println(String.format("DC1: GetEffectAmmount ID: %d Level: %d Base: %.2f Increase: %.2f Novice: %.2f Max: %.2f Min: %.2f " + "Exception: %s Exctpion Low: %.2f Exception High: %.2f Exception Value: %.2f Floor Result: %s", mID, skillLevel, mBase, mLevelIncrease,
					mLevelIncreaseNovice, mMax, mMin, mException, mExceptionLow, mExceptionHigh, mExceptionValue, mFloorResult));
		}
		return (mFloorResult ? Math.floor(effectAmount) : effectAmount);
	}

	public EffectType getEffectType() {
		return mType;
	}

	protected int getElfEffectLevel() {
		return mNormalLevel;
	}

	public int getId() {
		return mID;
	}

	public int getInitiatorId() {
		return mInitator.getTypeId();
	}

	public int getOutputId() {
		return mOutput.getTypeId();
	}

	public ItemStack getOutput() {
		return mOutput;
	}

	public ItemStack getOutput(DCPlayer player) {
		return getOutput(player, null);
	}

	public ItemStack getOutput(DCPlayer player, Byte oldData) {
		Byte data = (mOutput.getData() == null ? null : mOutput.getData().getData());

		if (data != null && data == 0)
			data = oldData;

		int count = Util.randomAmount(getEffectAmount(player));
		ItemStack item = new ItemStack(mOutput.getTypeId(), count, data);
		return item;
	}

	public boolean getToolRequired() {
		return mRequireTool;
	}

	public int[] getTools() {
		return mTools;
	}

	public boolean checkInitiator(ItemStack item) {
		if (item == null)
			return checkInitiator(0, (byte) 0);
		else
			return checkInitiator(item.getTypeId(), (byte) item.getDurability());
	}

	public boolean checkInitiator(int id, byte data) {
		if (mInitator.getTypeId() != id)
			return false;

		if (mInitator.getData() != null) {
			if (mInitator.getData().getData() == 0) // -1 means we dont care.
				return true;
			return mInitator.getData().getData() == data;
		}
		return true;
	}

	/**
	 * Tool to string parser for effect descriptions
	 * 
	 * @return
	 */
	private String toolType() {
		for (int toolId : mTools) {
			if (toolId == 267)
				return "swords";
			if (toolId == 292)
				return "hoes";
			if (toolId == 258)
				return "axes";
			if (toolId == 270)
				return "pickaxes";
			if (toolId == 257)
				return "most picks";
			if (toolId == 278)
				return "high picks";
			if (toolId == 256)
				return "shovels";
			if (toolId == 346)
				return "fishing rod";
		}
		return "any tool";
	}

	public boolean checkMob(Entity entity) {
		if (mCreature == null)
			return false;

		switch (mCreature) {
		case CHICKEN:
			return (entity instanceof Chicken);
		case COW:
			return (entity instanceof Cow);
		case CREEPER:
			return (entity instanceof Creeper);
		case GHAST:
			return (entity instanceof Ghast);
		case GIANT:
			return (entity instanceof Giant);
		case PIG:
			return (entity instanceof Pig);
		case PIG_ZOMBIE:
			return (entity instanceof PigZombie);
		case SHEEP:
			return (entity instanceof Sheep);
		case SKELETON:
			return (entity instanceof Skeleton);
		case SLIME:
			return (entity instanceof Slime);
		case SPIDER:
			return (entity instanceof Spider);
		case SQUID:
			return (entity instanceof Squid);
		case ZOMBIE:
			return (entity instanceof Zombie) && !(entity instanceof PigZombie);
		case WOLF:
			return (entity instanceof Wolf);
		case MUSHROOM_COW:
			return (entity instanceof MushroomCow);
		case SILVERFISH:
			return (entity instanceof Silverfish);
		case ENDERMAN:
			return (entity instanceof Enderman);
		case VILLAGER:
			return (entity instanceof Villager);
		case BLAZE:
			return (entity instanceof Blaze);
		case MAGMA_CUBE:
			return (entity instanceof MagmaCube);
		case CAVE_SPIDER:
			return (entity instanceof CaveSpider);
		case SNOWMAN:
			return (entity instanceof Snowman);
		case ENDER_DRAGON:
			return (entity instanceof EnderDragon);
		default:
			return false;
		}
	}

	public boolean checkTool(ItemStack tool) {
		if (!mRequireTool)
			return true;

		if (tool == null)
			return false;

		for (int id : mTools)
			if (id == tool.getTypeId())
				return true;

		return false;
	}

	@Override
	public String toString() {
		return Integer.toString(mID);
	}

	public void damageTool(DCPlayer player, int base, ItemStack tool) {
		damageTool(player, base, tool, true);
	}

	public void damageTool(DCPlayer player, int base, ItemStack tool, boolean negate) {
		short wear = (short) (Util.randomAmount(getEffectAmount(player)) * base);

		if (DwarfCraft.debugMessagesThreshold < 2)
			System.out.println(String.format("DC2: Affected durability of a \"%s\" - Effect: %d Old: %d Base: %d Wear: %d", Util.getCleanName(tool), mID, tool.getDurability(), base, wear));

		// Some code taken from net.minecraft.server.ItemStack line 165.
		// Checks to see if damage should be skipped.
		if (tool.containsEnchantment(Enchantment.DURABILITY)) {
			int level = tool.getEnchantmentLevel(Enchantment.DURABILITY);
			Random r = new Random();
			if (level > 0 && r.nextInt(level + 1) > 0) {
				return;
			}
		}

		base = (negate ? base : 0);

		if (wear == base)
			return; // This is normal wear, skip everything and let MC handle it
					// internally.

		tool.setDurability((short) (tool.getDurability() + wear - base));
		// This may have the side effect of causing items to flicker when they
		// are about to break
		// If this becomes a issue, we need to cast to a CraftItemStack, then
		// make CraftItemStack.item public,
		// And call CraftItemStack.item.damage(-base, player.getPlayer());

		if (tool.getDurability() >= tool.getType().getMaxDurability()) {
			if (tool.getTypeId() == 267 && tool.getDurability() < 250)
				return;

			if (tool.getAmount() > 1) {
				tool.setAmount(tool.getAmount() - 1);
				tool.setDurability((short) -1);
			} else {
				player.getPlayer().setItemInHand(null);
			}
		}
	}

}
