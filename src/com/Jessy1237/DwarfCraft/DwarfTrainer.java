package com.Jessy1237.DwarfCraft;

/**
 * Original Authors: smartaleq, LexManos and RCarretta
 */

import java.util.List;

import net.minecraft.server.v1_7_R1.EntityLiving;
import net.minecraft.server.v1_7_R1.EntityPlayer;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.sharesc.caliog.npclib.HumanNPC;

public final class DwarfTrainer {
	private HumanNPC mEntity;
	private Integer mSkillID;
	private Integer mMaxLevel;
	private Integer mMinLevel;
	private boolean mIsGreeter;
	private String mMsgID;
	private World mWorld;
	private Material mHeldItem;
	private String mName;
	private String mID;
	private final DwarfCraft plugin;
	private boolean wait;
	private long lastTrain;

	public DwarfTrainer(final DwarfCraft plugin, Location location, String uniqueId, String name, Integer skillId, Integer maxSkill, Integer minSkill, String greeterMessage, boolean isGreeter, boolean wait, long lastTrain) {

		this.plugin = plugin;
		this.mSkillID = skillId;
		this.mMaxLevel = maxSkill;
		this.mMinLevel = minSkill;
		this.mMsgID = greeterMessage;
		this.mIsGreeter = isGreeter;
		this.mWorld = location.getWorld();
		this.mName = name;
		this.mID = uniqueId;
		this.mEntity = (HumanNPC) plugin.getNPCManager().spawnHumanNPC(mName, location, uniqueId);
		this.wait = wait;
		this.lastTrain = lastTrain;
		this.getEntity().getEntity().yaw = location.getYaw();
		((EntityPlayer) this.getEntity().getEntity()).aP = location.getYaw();
		this.getEntity().getEntity().pitch = location.getPitch();

		if (mIsGreeter)
			mHeldItem = Material.AIR;
		else
			mHeldItem = plugin.getConfigManager().getGenericSkill(skillId).getTrainerHeldMaterial();

		assert (mHeldItem != null);

		if (mHeldItem != Material.AIR)
			mEntity.setItemInHand(mHeldItem);
	}

	@Override
	public boolean equals(Object that) {
		if (this == that)
			return true;
		else if (that instanceof HumanEntity)
			return (mEntity.getBukkitEntity().getEntityId() == ((HumanEntity) that).getEntityId());
		return false;
	}

	public HumanNPC getEntity() {
		return mEntity;
	}

	public Location getLocation() {
		return mEntity.getBukkitEntity().getLocation();
	}

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
		return mEntity.getName();
	}

	public Integer getSkillTrained() {
		return mSkillID;
	}

	public String getUniqueId() {
		return mID;
	}

	protected World getWorld() {
		return mWorld;
	}

	public boolean isGreeter() {
		return mIsGreeter;
	}

	public void lookAt(Entity target) {
		assert (target != null);
		Location l;
		l = target.getLocation().clone();
		if (target instanceof Player) {
			l.setY(l.getY() + ((Player) target).getEyeHeight());
		}
		this.lookAt(l);
		return;
	}

	public void lookAt(Player p, DwarfTrainer d) {
		if (p != null) {
			Location l = p.getEyeLocation().clone();
			d.getEntity().lookAtPoint(l);
			d.getLocation().setYaw((float)((EntityPlayer) d.getEntity().getEntity()).aP);
			d.getLocation().setPitch(d.getEntity().getEntity().pitch);
			plugin.getDataManager().updateTrainerLocation(d, (float)((EntityLiving) d.getEntity().getEntity()).aP, d.getEntity().getEntity().pitch);
		}
		return;
	}

	protected void lookAt(Location l) {
		assert (l != null);
		return;
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

	@SuppressWarnings("unused")
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
			if (!player.getInventory().contains(costStack.getTypeId())) {
				hasMats = false;
				plugin.getOut().sendMessage(player, String.format("&cAn additional &2%d %s &cis required", costStack.getAmount(), costStack.getType()), tag);
				continue;
			}

			for (ItemStack invStack : player.getInventory().getContents()) {
				if (invStack == null)
					continue;

				if (invStack.getTypeId() == costStack.getTypeId()) {
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
			plugin.getDataManager().saveDwarfData(dCPlayer);
		}
		setWait(false);
	}

	public void setDisplayName(String name) {
		Location loc = getLocation();
		plugin.getNPCManager().despawnHumanByName(this.mName);
		this.mName = name;
		this.mEntity = (HumanNPC) plugin.getNPCManager().spawnHumanNPC(mName, loc, this.mID);
		plugin.getDataManager().renameTrainer(this.mID, name);
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
}