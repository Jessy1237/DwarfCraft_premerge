package com.Jessy1237.DwarfCraft;

import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.AbstractNPC;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

public class DwarfTrainerTrait extends Trait
{
	
	private DwarfCraft	plugin;
	private Material	mHeldItem;
	@Persist(required = true)
	private int			mSkillID;
	@Persist(required = true)
	private int			mMaxLevel;
	@Persist(required = true)
	private int			mMinLevel;
	@Persist(required = true)
	private boolean		mIsGreeter;
	@Persist(required = true)
	private String		mMsgID;
	
	@Override
	public void onSpawn()
	{
		DwarfTrainer trainer = new DwarfTrainer(plugin,
				(AbstractNPC) getNPC());
		if (isGreeter())
			this.mHeldItem = Material.AIR;
		else
			this.mHeldItem = plugin.getConfigManager()
					.getGenericSkill(getSkillTrained())
					.getTrainerHeldMaterial();
		
		assert (this.mHeldItem != null);
		
		if (this.mHeldItem != Material.AIR)
			((LivingEntity) getNPC().getEntity()).getEquipment()
					.setItemInMainHand(new ItemStack(mHeldItem, 1));
		
		if (plugin.getDataManager() == null)
			System.out.println("1");
		
		plugin.getDataManager().trainerList.put(getNPC().getId(), trainer);
	}
	
	@Override
	public void onDespawn()
	{
		plugin.getDataManager().trainerList.remove(getNPC().getId());
	}
	
	public DwarfTrainerTrait()
	{
		super("DwarfTrainer");
		this.plugin = (DwarfCraft) Bukkit.getServer().getPluginManager()
				.getPlugin("DwarfCraft");
	}
	
	public DwarfTrainerTrait(DwarfCraft plugin, Integer ID, Integer skillID,
			Integer maxLevel, Integer minLevel, boolean isGreeter,
			String msgID)
	{
		super("DwarfTrainer");
		this.plugin = plugin;
		this.mSkillID = skillID;
		this.mMaxLevel = maxLevel;
		this.mMinLevel = minLevel;
		this.mIsGreeter = isGreeter;
		this.mMsgID = msgID;
	}
	
	@EventHandler
	public void onNPCLeftClick(NPCLeftClickEvent event)
	{
		if (event.getNPC().hasTrait(DwarfTrainerTrait.class)
				&& event.getNPC().getId() == getNPC().getId())
		{
			plugin.getDCEntityListener().onNPCLeftClickEvent(event);
		}
	}
	
	@EventHandler
	public void onNPCRightClick(NPCRightClickEvent event)
	{
		if (event.getNPC().hasTrait(DwarfTrainerTrait.class)
				&& event.getNPC().getId() == getNPC().getId())
		{
			plugin.getDCEntityListener().onNPCRightClickEvent(event);
		}
	}
	
	public int getMaxSkill()
	{
		return this.mMaxLevel;
	}
	
	public int getMinSkill()
	{
		return this.mMinLevel;
	}
	
	public boolean isGreeter()
	{
		return this.mIsGreeter;
	}
	
	protected String getMessage()
	{
		return this.mMsgID;
	}
	
	public int getSkillTrained()
	{
		return this.mSkillID;
	}
	
	@SuppressWarnings("deprecation")
	public int getMaterial()
	{
		if (this.mHeldItem != null)
			return this.mHeldItem.getId();
		else
			return (Material.AIR.getId());
	}
}
