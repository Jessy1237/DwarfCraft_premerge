package com.Jessy1237.DwarfCraft;

/**
 * Original Authors: smartaleq, LexManos and RCarretta
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class DCPlayer
{
	private final DwarfCraft		plugin;
	private HashMap<Integer, Skill>	skills;
	private Player					player;
	private String					race;
	
	public void setPlayer(Player player)
	{
		this.player = player;
	}
	
	public DCPlayer(final DwarfCraft plugin, Player whoami)
	{
		this.plugin = plugin;
		this.player = whoami;
		this.race = plugin.getConfigManager().getDefaultRace();
		this.skills = plugin.getConfigManager().getAllSkills();
	}
	
	public DCPlayer(final DwarfCraft plugin, Player whoami, String race)
	{
		this.plugin = plugin;
		this.player = whoami;
		this.race = race;
		this.skills = plugin.getConfigManager().getAllSkills();
	}
	
	protected List<List<ItemStack>> calculateTrainingCost(Skill skill)
	{
		int highSkills = countHighSkills();
		int dwarfLevel = getDwarfLevel();
		int quartileSize = Math.min(4, highSkills / 4);
		int quartileNumber = 1; // 1 = top, 2 = 2nd, etc.
		int[] levelList = new int[highSkills + 1];
		List<ItemStack> costToLevelStack = new ArrayList<ItemStack>();
		List<ItemStack> totalCostStack = new ArrayList<ItemStack>();
		int i = 0;
		
		// Creates an ordered list of skill levels and finds where in that
		// list
		// the skill is (what quartile)
		if (DwarfCraft.debugMessagesThreshold < 0)
			System.out.println("DC0: starting skill ordering for quartiles");
		for (Skill s : getSkills().values())
		{
			if (s.getLevel() > 5)
			{
				levelList[i] = s.getLevel();
				i++;
			}
		}
		Arrays.sort(levelList);
		if (levelList[highSkills - quartileSize] <= skill.getLevel())
			quartileNumber = 1;
		else if (levelList[highSkills - 2 * quartileSize] <= skill.getLevel())
			quartileNumber = 2;
		else if (levelList[highSkills - 3 * quartileSize] <= skill.getLevel())
			quartileNumber = 3;
		if (skill.getLevel() < 5)
			quartileNumber = 1; // low skills train full speed
			
		// calculate quartile penalties for 2nd/3rd/4th quartile
		double multiplier = Math.max(1,
				Math.pow(1.072, (skill.getLevel() - 5)));
		if (quartileNumber == 2)
			multiplier *= (1 + 1 * dwarfLevel / (100 + 3 * dwarfLevel));
		if (quartileNumber == 3)
			multiplier *= (1 + 2 * dwarfLevel / (100 + 3 * dwarfLevel));
		if (quartileNumber == 4)
			multiplier *= (1 + 3 * dwarfLevel / (100 + 3 * dwarfLevel));
		
		// create output item stack of new items
		int item1Amount = ((int) Math.min(
				Math.ceil((skill.getLevel() + 1) * skill.Item1.Base
						* multiplier - .01), skill.Item1.Max)), item2Amount = ((int) Math
				.min(Math.ceil((skill.getLevel() + 1) * skill.Item2.Base
						* multiplier - .01), skill.Item2.Max)), item3Amount = ((int) Math
				.min(Math.ceil((skill.getLevel() + 1) * skill.Item3.Base
						* multiplier - .01), skill.Item3.Max));
		
		totalCostStack.add(0, new ItemStack(skill.Item1.Item.getType(),
				item1Amount, skill.Item1.Item.getDurability()));
		costToLevelStack.add(
				0,
				new ItemStack(skill.Item1.Item.getType(), item1Amount
						- skill.getDeposit1(), skill.Item1.Item
						.getDurability()));
		
		if (skill.Item2.Item.getType() != Material.AIR)
		{
			totalCostStack.add(1, new ItemStack(skill.Item2.Item.getType(),
					item2Amount, skill.Item2.Item.getDurability()));
			costToLevelStack.add(
					1,
					new ItemStack(skill.Item2.Item.getType(), item2Amount
							- skill.getDeposit2(), skill.Item2.Item
							.getDurability()));
		}
		if (skill.Item3.Item.getType() != Material.AIR)
		{
			totalCostStack.add(2, new ItemStack(skill.Item3.Item.getType(),
					item3Amount, skill.Item3.Item.getDurability()));
			costToLevelStack.add(
					2,
					new ItemStack(skill.Item3.Item.getType(), item3Amount
							- skill.getDeposit3(), skill.Item3.Item
							.getDurability()));
		}
		List<List<ItemStack>> costs = new ArrayList<List<ItemStack>>();
		costs.add(0, costToLevelStack);
		costs.add(1, totalCostStack);
		return costs;
	}
	
	/**
	 * Counts skills greater than level 5, used for training costs
	 */
	private int countHighSkills()
	{
		int highCount = 0;
		for (Skill s : getSkills().values())
		{
			if (s.getLevel() > 5)
				highCount++;
		}
		return highCount;
	}
	
	/**
	 * Calculates the dwarf's total level for display/e-peening. Value is the
	 * total of all skill level above 5, or the highest skill level when none
	 * are above 5.
	 * 
	 * @return
	 */
	public int getDwarfLevel()
	{
		int playerLevel = 5;
		int highestSkill = 0;
		for (Skill s : getSkills().values())
		{
			if (s.getLevel() > highestSkill)
				highestSkill = s.getLevel();
			
			if (s.getLevel() > 5)
				playerLevel = playerLevel + s.getLevel() - 5;
		}
		if (playerLevel == 5)
			playerLevel = highestSkill;
		return playerLevel;
	}
	
	/**
	 * Retrieves an effect from a player based on its effectId.
	 * 
	 * @param effectId
	 * @return
	 */
	protected Effect getEffect(int effectId)
	{
		Skill skill = getSkill(effectId / 10);
		for (Effect effect : skill.getEffects())
		{
			if (effect.getId() == effectId)
				return effect;
		}
		return null;
	}
	
	public Player getPlayer()
	{
		return player;
	}
	
	/**
	 * Gets a dwarf's skill from an effect
	 * 
	 * @param effect
	 *            (does not have to be this dwarf's effect, only used for ID#)
	 * @return Skill or null if none found
	 */
	protected Skill getSkill(Effect effect)
	{
		for (Skill skill : skills.values())
		{
			if (skill.getId() == effect.getId() / 10)
				return skill;
		}
		return null;
	}
	
	/**
	 * Gets a dwarf's skill by id
	 * 
	 * @param skillId
	 * @return Skill or null if none found
	 */
	public Skill getSkill(int skillId)
	{
		Skill skill = skills.get(skillId);
		return skill;
	}
	
	/**
	 * Gets a dwarf's skill by name or id number(as String)
	 * 
	 * @param skillName
	 * @return Skill or null if none found
	 */
	protected Skill getSkill(String skillName)
	{
		try
		{
			return getSkill(Integer.parseInt(skillName));
		} catch (NumberFormatException n)
		{
			for (Skill skill : getSkills().values())
			{
				if (skill.getDisplayName() == null)
					continue;
				if (skill.getDisplayName().equalsIgnoreCase(skillName))
					return skill;
				if (skill.toString().equalsIgnoreCase(skillName))
					return skill;
				if (skill.getDisplayName().toLowerCase()
						.regionMatches(0, skillName.toLowerCase(), 0, 8))
					return skill;
				if (skill.toString().toLowerCase()
						.regionMatches(0, skillName.toLowerCase(), 0, 8))
					return skill;
			}
			
		}
		return null;
	}
	
	public HashMap<Integer, Skill> getSkills()
	{
		return skills;
	}
	
	/**
	 * Calculates the Dwarf's total Level
	 * 
	 * @return total level
	 */
	public int level()
	{
		int playerLevel = 5;
		int highestSkill = 0;
		for (Skill s : getSkills().values())
		{
			if (s.getLevel() > highestSkill)
				highestSkill = s.getLevel();
			if (s.getLevel() > 5)
				playerLevel += s.getLevel() - 5;
			;
		}
		if (playerLevel == 5)
			playerLevel = highestSkill;
		return playerLevel;
	}
	
	/**
	 * @param skills
	 *            the skills to set
	 */
	protected void setSkills(HashMap<Integer, Skill> skills)
	{
		this.skills = skills;
	}
	
	public int getSkillLevel(int id)
	{
		for (Skill s : getSkills().values())
			if (s.getId() == id)
				return s.getLevel();
		return 0;
	}
	
	public void changeRace(String race)
	{
		final String oldRace = this.race;
		this.race = race;
		skills = plugin.getConfigManager().getAllSkills();
		Skill[] dCSkills = new Skill[skills.size()];
		
		// Resets the players skills
		int I = 0;
		for (Skill skill : skills.values())
		{
			skill.setLevel(0);
			skill.setDeposit1(0);
			skill.setDeposit2(0);
			skill.setDeposit3(0);
			dCSkills[I] = skill;
			I++;
		}
		
		// Resets the players prefix
		if (plugin.isChatEnabled())
			if (plugin.getChat().getPlayerPrefix(getPlayer())
					.contains(plugin.getUtil().getPlayerPrefix(oldRace)))
				plugin.getChat().setPlayerPrefix(
						getPlayer(),
						plugin.getChat()
								.getPlayerPrefix(getPlayer())
								.replace(
										plugin.getUtil().getPlayerPrefix(
												oldRace),
										plugin.getUtil()
												.getPlayerPrefix(this)));
		
		plugin.getDataManager().saveDwarfData(this, dCSkills);
	}
	
	public String getRace()
	{
		return race;
	}
	
	public void setRace(String race)
	{
		this.race = race;
	}
}
