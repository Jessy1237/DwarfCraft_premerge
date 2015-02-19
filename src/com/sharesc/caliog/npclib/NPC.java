package com.sharesc.caliog.npclib;

import net.minecraft.server.v1_8_R1.EntityPlayer;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

public class NPC {
	private final net.minecraft.server.v1_8_R1.Entity entity;

	public NPC(net.minecraft.server.v1_8_R1.Entity entity) {
		this.entity = entity;
	}

	public net.minecraft.server.v1_8_R1.Entity getEntity() {
		return this.entity;
	}

	public void removeFromWorld() {
		try {
			this.entity.world.removeEntity(this.entity);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public org.bukkit.entity.Entity getBukkitEntity() {
		return this.entity.getBukkitEntity();
	}

	public void moveTo(Location l) {
		getBukkitEntity().teleport(l);
	}

	public EntityEquipment getEquipment() {
		return ((LivingEntity) getEntity()).getEquipment();
	}

	public ItemStack getEquipment(int slot) {
		switch (slot) {
		case 0:
			return getEquipment().getItemInHand();
		case 1:
			return getEquipment().getHelmet();
		case 2:
			return getEquipment().getChestplate();
		case 3:
			return getEquipment().getLeggings();
		case 4:
			return getEquipment().getBoots();
		default:
			return null;
		}
	}

	public void setYaw(float yaw) {
		getEntity().yaw = yaw;
		((EntityPlayer) getEntity()).aI = yaw;
		((EntityPlayer) getEntity()).aJ = yaw;
	}
}
