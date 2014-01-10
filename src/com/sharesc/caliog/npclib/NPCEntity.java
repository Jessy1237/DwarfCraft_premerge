package com.sharesc.caliog.npclib;

import net.minecraft.server.v1_7_R1.Entity;
import net.minecraft.server.v1_7_R1.EntityHuman;
import net.minecraft.server.v1_7_R1.EntityPlayer;
import net.minecraft.server.v1_7_R1.EnumGamemode;
import net.minecraft.server.v1_7_R1.PacketPlayOutBed;
import net.minecraft.server.v1_7_R1.PlayerInteractManager;
import net.minecraft.server.v1_7_R1.WorldServer;
import net.minecraft.util.com.mojang.authlib.GameProfile;

import org.bukkit.craftbukkit.v1_7_R1.CraftServer;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftEntity;
import org.bukkit.event.entity.EntityTargetEvent;

/**
 * 
 * @author martin
 */
public class NPCEntity extends EntityPlayer {

  private int lastTargetId;
  private long lastBounceTick;
  private int lastBounceId;
  private boolean isSleeping;

	public NPCEntity(NPCManager npcManager, BWorld world, GameProfile s,
			PlayerInteractManager playerInteractManager) {
		super(npcManager.getServer().getMCServer(), world.getWorldServer(), s,
				playerInteractManager);

		playerInteractManager.b(EnumGamemode.SURVIVAL);

		playerConnection = new NPCNetHandler(npcManager, this);
		lastTargetId = -1;
		lastBounceId = -1;
		lastBounceTick = 0;

		fauxSleeping = true;
	}

	public void setBukkitEntity(org.bukkit.entity.Entity entity) {
		// TODO
		bukkitEntity = (CraftEntity) entity;
	}

	@Override
	public boolean a(EntityHuman entity) {
		EntityTargetEvent event = new NPCEntityTargetEvent(getBukkitEntity(),
				entity.getBukkitEntity(),
				NPCEntityTargetEvent.NPCTargetReason.NPC_RIGHTCLICKED);
		CraftServer server = ((WorldServer) world).getServer();
		server.getPluginManager().callEvent(event);
		return super.a(entity);
	}

	public void b_(EntityHuman entity) {
		if ((lastBounceId != entity.getId() || System.currentTimeMillis()
				- lastBounceTick > 1000)
				&& entity.getBukkitEntity().getLocation()
						.distanceSquared(getBukkitEntity().getLocation()) <= 1) {
			EntityTargetEvent event = new NPCEntityTargetEvent(
					getBukkitEntity(), entity.getBukkitEntity(),
					NPCEntityTargetEvent.NPCTargetReason.NPC_BOUNCED);
			CraftServer server = ((WorldServer) world).getServer();
			server.getPluginManager().callEvent(event);

			lastBounceTick = System.currentTimeMillis();
			lastBounceId = entity.getId();
		}

		if (lastTargetId == -1 || lastTargetId != entity.getId()) {
			EntityTargetEvent event = new NPCEntityTargetEvent(
					getBukkitEntity(), entity.getBukkitEntity(),
					NPCEntityTargetEvent.NPCTargetReason.CLOSEST_PLAYER);
			CraftServer server = ((WorldServer) world).getServer();
			server.getPluginManager().callEvent(event);
			lastTargetId = entity.getId();
		}

		super.b_(entity);// TODO c_ renamed to b_ ?! TEST
	}

	@Override
	public void c(Entity entity) {
		if (lastBounceId != entity.getId()
				|| System.currentTimeMillis() - lastBounceTick > 1000) {
			EntityTargetEvent event = new NPCEntityTargetEvent(
					getBukkitEntity(), entity.getBukkitEntity(),
					NPCEntityTargetEvent.NPCTargetReason.NPC_BOUNCED);
			CraftServer server = ((WorldServer) world).getServer();
			server.getPluginManager().callEvent(event);

			lastBounceTick = System.currentTimeMillis();
		}

		lastBounceId = entity.getId();
		super.c(entity);
	}

	@Override
	public void move(double arg0, double arg1, double arg2) {
		setPosition(arg0, arg1, arg2);
	}
	
	public void setSleeping(boolean sleep) {
		  if (sleep) {
		    this.sleeping = true;
		    this.isSleeping = true;
		    PacketPlayOutBed packetplayoutbed = new PacketPlayOutBed(this, 22, 162, 187);

		    r().getTracker().a(this, packetplayoutbed);
		    this.playerConnection.a(this.locX, this.locY, this.locZ, this.yaw, this.pitch);
		    this.playerConnection.sendPacket(packetplayoutbed);
		    a(1.0F, 2.0F);
		  }
		  else {
		    this.playerConnection.player.a(false, true, true);
		    this.sleeping = false;
		    this.isSleeping = false;
		  }
	}
	
	public boolean isSleeping(){
		return isSleeping;
	}

}
