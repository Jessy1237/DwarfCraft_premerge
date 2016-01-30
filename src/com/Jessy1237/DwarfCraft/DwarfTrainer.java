package com.Jessy1237.DwarfCraft;

/**
 * Original Authors: smartaleq, LexManos and RCarretta
 */

import java.util.ArrayList;
import java.util.List;

import net.citizensnpcs.api.npc.AbstractNPC;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class DwarfTrainer {
	private AbstractNPC mEntity;
	private final DwarfCraft plugin;
	private boolean wait;
	private long lastTrain;

	public DwarfTrainer(final DwarfCraft plugin, AbstractNPC mEntity) {
		this.plugin = plugin;
		this.mEntity = mEntity;
		this.wait = false;
		this.lastTrain = 0;
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

	protected int getMaterial() {
		return mEntity.getTrait(DwarfTrainerTrait.class).getMaterial();
	}

	public World getWorld() {
		return mEntity.getStoredLocation().getWorld();
	}
	
	public int getMaxSkill() {
		return mEntity.getTrait(DwarfTrainerTrait.class).getMaxSkill();
	}

	public Integer getMinSkill() {
		return mEntity.getTrait(DwarfTrainerTrait.class).getMinSkill();
	}

	protected String getMessage() {
		return mEntity.getTrait(DwarfTrainerTrait.class).getMessage();
	}

	public String getName() {
		return mEntity.getName();
	}

	public int getSkillTrained() {
		return mEntity.getTrait(DwarfTrainerTrait.class).getSkillTrained();
	}

	public int getUniqueId() {
		return mEntity.getId();
	}


	public boolean isGreeter() {
		return mEntity.getTrait(DwarfTrainerTrait.class).isGreeter();
	}

	public void printLeftClick(Player player) {
		GreeterMessage msg = plugin.getDataManager().getGreeterMessage(getMessage());
		if (msg != null) {
			plugin.getOut().sendMessage(player, msg.getLeftClickMessage());
		} else {
			System.out.println(String.format("[DC] Error: Greeter %s has no left click message. Check your configuration file for message ID %d", getUniqueId(), getMessage()));
		}
		return;
	}

	public void printRightClick(Player player) {
		GreeterMessage msg = plugin.getDataManager().getGreeterMessage(getMessage());
		if (msg != null) {
			plugin.getOut().sendMessage(player, msg.getRightClickMessage());
		}
		return;
	}

	@SuppressWarnings({ "unused", "deprecation" })
	public void trainSkill(DCPlayer dCPlayer) {
		Skill skill = dCPlayer.getSkill(getSkillTrained());
		Player player = dCPlayer.getPlayer();
		String tag = String.format("&6[Train &b%d&6] ", skill.getId());

		if(dCPlayer.getRace().equalsIgnoreCase("NULL")) {
			plugin.getOut().sendMessage(player, "&cPlease choose a race!");
			setWait(false);
			return;
		}
		
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

		if (skill.getLevel() >= plugin.getConfigManager().getMaxSkillLevel()) {
			plugin.getOut().sendMessage(player, "&cYour skill is max level (" + plugin.getConfigManager().getMaxSkillLevel() + ")!", tag);
			setWait(false);
			return;
		}

		if (skill.getLevel() >= getMaxSkill()) {
			plugin.getOut().sendMessage(player, "&cI can't teach you any more, find a higher level trainer", tag);
			setWait(false);
			return;
		}

		if (skill.getLevel() < getMinSkill()) {
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
				plugin.getOut().sendMessage(player, String.format("&aNo more &2%s &ais needed", Util.getCleanName(costStack)), tag);
				continue;
			}
			if (!player.getInventory().contains(costStack.getTypeId())) {
				if (Util.checkEquivalentBuildBlocks(costStack.getTypeId(), -1) != null) {
					ArrayList<Integer> i = Util.checkEquivalentBuildBlocks(costStack.getTypeId(), -1);
					boolean contains = false;
					for(int id : i) {
						if(player.getInventory().contains(id)) {
							contains = true;
						}
					}
					if(!contains) {
						hasMats = false;
						plugin.getOut().sendMessage(player, String.format("&cAn additional &2%d %s &cis required", costStack.getAmount(), Util.getCleanName(costStack)), tag);
						continue;
					}
				} else {
					hasMats = false;
					plugin.getOut().sendMessage(player, String.format("&cAn additional &2%d %s &cis required", costStack.getAmount(), Util.getCleanName(costStack)), tag);
					continue;
				}
			}

			for (ItemStack invStack : player.getInventory().getContents()) {
				if (invStack == null)
					continue;
				if ((invStack.getTypeId() == costStack.getTypeId() && (invStack.getDurability() == costStack.getDurability() || (Util.isTool(invStack.getTypeId()) && invStack.getDurability() == invStack.getType().getMaxDurability())))
						|| Util.checkEquivalentBuildBlocks(invStack.getTypeId(), costStack.getTypeId()) != null) {
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

					if (costStack.getType().equals(skill.Item1.Item.getType())) {
						skill.setDeposit1(skill.getDeposit1() + delta);
					} else if (costStack.getType().equals(skill.Item2.Item.getType())) {
						skill.setDeposit2(skill.getDeposit2() + delta);
					} else {
						skill.setDeposit3(skill.getDeposit3() + delta);
					}
				}
			}
			if (costStack.getAmount() == 0) {
				plugin.getOut().sendMessage(player, String.format("&aNo more &2%s &ais needed", Util.getCleanName(costStack)), tag);
			} else {
				plugin.getOut().sendMessage(player, String.format("&cAn additional &2%d %s &c is required", costStack.getAmount(), Util.getCleanName(costStack)), tag);
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

	public boolean isWaiting() {
		return this.wait;
	}
	
	public long getLastTrain() {
		return this.lastTrain;
	}
	
	public void setWait(boolean wait) {
		this.wait = wait;
	}
	
	public void setLastTrain(long lastTrain) {
		this.lastTrain = lastTrain;
	}

	public String getType() {
		return mEntity.getEntity().getType().toString();
	}
}