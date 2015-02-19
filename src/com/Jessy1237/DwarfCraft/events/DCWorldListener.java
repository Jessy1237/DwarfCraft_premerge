package com.Jessy1237.DwarfCraft.events;

/**
 * Original Authors: smartaleq, LexManos and RCarretta
 */

import net.minecraft.server.v1_8_R1.EntityPlayer;
import net.minecraft.server.v1_8_R1.EnumPlayerInfoAction;
import net.minecraft.server.v1_8_R1.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_8_R1.PacketPlayOutPlayerInfo;

import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import com.Jessy1237.DwarfCraft.DwarfCraft;
import com.Jessy1237.DwarfCraft.DwarfTrainer;
import com.sharesc.caliog.npclib.NPCUtils;

public class DCWorldListener implements Listener {
	private final DwarfCraft plugin;

	public DCWorldListener(final DwarfCraft plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onChunkUnload(ChunkUnloadEvent event) {
		Chunk chunk = event.getChunk();
		event.setCancelled(plugin.getDataManager().checkTrainersInChunk(chunk));
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onChunkLoad(ChunkLoadEvent event) {
		for(Entity e : event.getChunk().getEntities()) {
			if(e instanceof HumanEntity) {
				DwarfTrainer t = plugin.getDataManager().getTrainer(e);
				if(t != null) {
					NPCUtils.sendPacketNearby(t.getLocation(), new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, (EntityPlayer)t.getEntity().getEntity()));
					NPCUtils.sendPacketNearby(t.getLocation(), new PacketPlayOutNamedEntitySpawn((EntityPlayer)t.getEntity().getEntity()));
				}
			}
		}
	}
}