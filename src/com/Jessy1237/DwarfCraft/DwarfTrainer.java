package com.Jessy1237.DwarfCraft;

/**
 * Original Authors: smartaleq, LexManos and RCarretta
 */

import java.util.List;
import java.util.UUID;

import net.citizensnpcs.api.npc.AbstractNPC;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class DwarfTrainer {
	private AbstractNPC mEntity;
	private Integer mSkillID;
	private Integer mMaxLevel;
	private Integer mMinLevel;
	private boolean mIsGreeter;
	private String mMsgID;
	private World mWorld;
	private Material mHeldItem;
	private String mName;
	private int mID;
	private final DwarfCraft plugin;
	private boolean wait;
	private long lastTrain;

	public DwarfTrainer(final DwarfCraft plugin, Location location, int id, String name, Integer skillId, Integer maxSkill, Integer minSkill, String greeterMessage, boolean isGreeter, boolean wait, long lastTrain) {

		this.plugin = plugin;
		this.mSkillID = skillId;
		this.mMaxLevel = maxSkill;
		this.mMinLevel = minSkill;
		this.mMsgID = greeterMessage;
		this.mIsGreeter = isGreeter;
		this.mWorld = location.getWorld();
		this.mName = name;
		this.mEntity = (AbstractNPC) plugin.getNPCRegistry().createNPC(EntityType.PLAYER, UUID.randomUUID(), id, name);
		mEntity.spawn(location);
		mEntity.addTrait(DwarfTrainerTrait.class);
		mEntity.setProtected(true);
		this.wait = wait;
		this.lastTrain = lastTrain;
		this.mID = mEntity.getId();
		this.wait = wait;
		this.lastTrain = lastTrain;

		if (mIsGreeter)
			mHeldItem = Material.AIR;
		else
			mHeldItem = plugin.getConfigManager().getGenericSkill(skillId).getTrainerHeldMaterial();

		assert (mHeldItem != null);

		if (mHeldItem != Material.AIR)
			((LivingEntity)mEntity.getEntity()).getEquipment().setItemInHand(new ItemStack(mHeldItem, 1));

	}

	@Override
	public boolean equals(Object that) {
		if (this == that)
			return true;
		else if (that instanceof HumanEntity)
			return (mEntity.getId() == ((HumanEntity) that).getEntityId());
		return false;
	}

	public AbstractNPC getEntity() {
		return mEntity;
	}

	public Location getLocation() {
		return mEntity.getStoredLocation();
	}

	@SuppressWarnings("deprecation")
	protected int getMaterial() {
		if (mHeldItem != null)
			return mHeldItem.getId();
		else
			return (Material.AIR.getId());
	}

	public Integer getMaxSkill() {
		return mMaxLevel;
	}

	public Integer getMinSkill() {
		return mMinLevel;
	}

	protected String getMessage() {
		return mMsgID;
	}

	public String getName() {
		return mName;
	}

	public Integer getSkillTrained() {
		return mSkillID;
	}

	public int getUniqueId() {
		return mID;
	}

	protected World getWorld() {
		return mWorld;
	}

	public boolean isGreeter() {
		return mIsGreeter;
	}

	public void printLeftClick(Player player) {
		GreeterMessage msg = plugin.getDataManager().getGreeterMessage(mMsgID);
		if (msg != null) {
			plugin.getOut().sendMessage(player, msg.getLeftClickMessage());
		} else {
			System.out.println(String.format("[DC] Error: Greeter %s has no left click message. Check your configuration file for message ID %d", getUniqueId(), mMsgID));
		}
		return;
	}

	public void printRightClick(Player player) {
		GreeterMessage msg = plugin.getDataManager().getGreeterMessage(mMsgID);
		if (msg != null) {
			plugin.getOut().sendMessage(player, msg.getRightClickMessage());
		}
		return;
	}

	@SuppressWarnings({ "unused", "deprecation" })
	public void trainSkill(DCPlayer dCPlayer) {
		Skill skill = dCPlayer.getSkill(mSkillID);
		Player player = dCPlayer.getPlayer();
		String tag = String.format("&6[Train &b%d&6] ", skill.getId());

		if (skill == null) {
			plugin.getOut().sendMessage(player, "&cYour race doesn't have this skill!", tag);
			setWait(false);
			return;
		}

		if (skill.getLevel() >= 5 && !plugin.getConfigManager().getAllSkills(dCPlayer.getRace()).contains(skill.getId())) {
			plugin.getOut().sendMessage(player, "&cYour race doesn't specialize in this skill! Max level is (5)!");
			setWait(false);
			return;
		}

		if (skill.getLevel() >= 30) {
			plugin.getOut().sendMessage(player, "&cYour skill is max level (30)!", tag);
			setWait(false);
			return;
		}

		if (skill.getLevel() >= mMaxLevel) {
			plugin.getOut().sendMessage(player, "&cI can't teach you any more, find a higher level trainer", tag);
			setWait(false);
			return;
		}

		if (skill.getLevel() < mMinLevel) {
			plugin.getOut().sendMessage(player, "&cI can't teach a low level like you, find a lower level trainer", tag);
			setWait(false);
			return;
		}

		List<List<ItemStack>> costs = dCPlayer.calculateTrainingCost(skill);
		List<ItemStack> trainingCostsToLevel = costs.get(0);
		// List<ItemStack> totalCostsToLevel = costs.get(1);

		boolean hasMats = true;
		boolean deposited = false;
		for (ItemStack costStack : trainingCostsToLevel) {
			if (costStack == null) {
				continue;
			}
			if (costStack.getAmount() == 0) {
				plugin.getOut().sendMessage(player, String.format("&aNo more &2%s &ais needed", costStack.getType()), tag);
				continue;
			}
			if (!player.getInventory().contains(costStack.getTypeId()) && !(costStack.getTypeId() == 17 && player.getInventory().contains(162)) && !(costStack.getTypeId() == 162 && player.getInventory().contains(17))) {
				hasMats = false;
				plugin.getOut().sendMessage(player, String.format("&cAn additional &2%d %s &cis required", costStack.getAmount(), costStack.getType()), tag);
				continue;
			}

			for (ItemStack invStack : player.getInventory().getContents()) {
				if (invStack == null)
					continue;

				if (invStack.getTypeId() == costStack.getTypeId() || (invStack.getTypeId() == 162 && costStack.getTypeId() == 17) || (invStack.getTypeId() == 17 && costStack.getTypeId() == 162)) {
					deposited = true;
					int inv = invStack.getAmount();
					int cost = costStack.getAmount();
					int delta;
					if (cost - inv >= 0) {
						costStack.setAmount(cost - inv);
						player.getInventory().removeItem(invStack);
						delta = inv;
					} else {
						costStack.setAmount(0);
						invStack.setAmount(inv - cost);
						delta = cost;
					}

					if (costStack.getType().equals(skill.Item1.Item)) {
						skill.setDeposit1(skill.getDeposit1() + delta);
					} else if (costStack.getType().equals(skill.Item2.Item)) {
						skill.setDeposit2(skill.getDeposit2() + delta);
					} else {
						skill.setDeposit3(skill.getDeposit3() + delta);
					}
				}
			}
			if (costStack.getAmount() == 0) {
				plugin.getOut().sendMessage(player, String.format("&aNo more &2%s &ais needed", costStack.getType()), tag);
			} else {
				plugin.getOut().sendMessage(player, String.format("&cAn additional &2%d %s &c is required", costStack.getAmount(), costStack.getType()), tag);
				hasMats = false;
				deposited = true;
			}

		}

		if (hasMats) {
			skill.setLevel(skill.getLevel() + 1);
			skill.setDeposit1(0);
			skill.setDeposit2(0);
			skill.setDeposit3(0);
			plugin.getOut().sendMessage(player, "&6Training Successful!", tag);
		}
		if (deposited || hasMats) {
			Skill[] dCSkills = new Skill[1];
			dCSkills[0] = skill;
			plugin.getDataManager().saveDwarfData(dCPlayer, dCSkills);
		}

		setWait(false);
	}

	public void setDisplayName(String name) {
		this.mEntity.setName(name);
	}

	public boolean isWaiting() {
		return this.wait;
	}

	public void setWait(boolean wait) {
		this.wait = wait;
	}

	public long getLastTrain() {
		return this.lastTrain;
	}

	public void setLastTrain(long lastTrain) {
		this.lastTrain = lastTrain;
	}

	public void lookAt(Player p) {
		this.mEntity.faceLocation(p.getLocation());
		plugin.getDataManager().updateTrainerLocation(this, this.getLocation().getYaw(), this.getLocation().getPitch());
	}
}